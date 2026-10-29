package club.iuna.compat.permissions.fabric;

import club.iuna.compat.permissions.PermissionBridge;
import club.iuna.compat.permissions.Verdict;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Reflection adapter for {@code fabric-permissions-api} (package {@code me.lucko.fabric.api.permissions.v0}).
 *
 * <p>The API is not a compile-time dependency. Its classes live in a mod that Sinytra Connector
 * loads, in whatever version the Fabric mods of the pack embed, and this mod must keep loading
 * (and say why it is idle) when they are absent or changed. So the adapter looks the four
 * listener interfaces up by name, checks that their shape is exactly what the bridge expects,
 * and registers {@link Proxy} listeners on their {@code Event}s.
 *
 * <p>The shape check is the early warning for a breaking change: any difference is reported in
 * {@link Detection#problems()} and the affected event is simply not registered.
 */
public final class FabricPermissionsApi {

    /** The four listener interfaces and the single method each one declares. */
    public enum Kind {
        SOURCE_CHECK("PermissionCheckEvent", "onPermissionCheck"),
        SOURCE_OPTION("OptionRequestEvent", "onOptionRequest"),
        OFFLINE_CHECK("OfflinePermissionCheckEvent", "onPermissionCheck"),
        OFFLINE_OPTION("OfflineOptionRequestEvent", "onOptionRequest");

        public final String interfaceName;
        public final String methodName;

        Kind(String interfaceName, String methodName) {
            this.interfaceName = interfaceName;
            this.methodName = methodName;
        }

        boolean online() {
            return this == SOURCE_CHECK || this == SOURCE_OPTION;
        }
    }

    /** Where the classes the adapter depends on are found. Tests substitute their own. */
    public record Names(String apiPackage, String eventClass, String triStateClass) {
        public static final Names FABRIC = new Names(
                "me.lucko.fabric.api.permissions.v0",
                "net.fabricmc.fabric.api.event.Event",
                "net.fabricmc.fabric.api.util.TriState");
    }

    /**
     * Outcome of looking for the API.
     *
     * @param api      the adapter, present if at least the main {@code PermissionCheckEvent} is usable
     * @param absent   true when the API is not installed at all (not a problem, just nothing to do)
     * @param problems everything that differs from the expected shape
     */
    public record Detection(Optional<FabricPermissionsApi> api, boolean absent, List<String> problems) {
    }

    private record Hook(Kind kind, Class<?> type, Object event, Method method) {
    }

    private final Class<?> eventType;
    private final Method register;
    private final Method invoker;
    private final Map<Verdict, Object> triStates;
    private final Map<Kind, Hook> hooks;
    private final List<Kind> registered = new ArrayList<>();

    private FabricPermissionsApi(Class<?> eventType, Method register, Method invoker,
                                 Map<Verdict, Object> triStates, Map<Kind, Hook> hooks) {
        this.eventType = eventType;
        this.register = register;
        this.invoker = invoker;
        this.triStates = triStates;
        this.hooks = hooks;
    }

    /**
     * @param loader     the class loader that sees the Fabric mods (in practice the mod's own)
     * @param names      class names to look up
     * @param sourceType the type the source listeners must receive ({@code SharedSuggestionProvider})
     */
    public static Detection detect(ClassLoader loader, Names names, Class<?> sourceType) {
        List<String> problems = new ArrayList<>();
        Class<?> mainInterface = load(loader, names.apiPackage() + "." + Kind.SOURCE_CHECK.interfaceName);
        if (mainInterface == null) {
            return new Detection(Optional.empty(), true, List.of());
        }

        Class<?> eventType = load(loader, names.eventClass());
        Class<?> triStateType = load(loader, names.triStateClass());
        if (eventType == null || triStateType == null) {
            problems.add("fabric-permissions-api is present but " + (eventType == null ? names.eventClass() : names.triStateClass())
                    + " is not visible from this mod");
            return new Detection(Optional.empty(), false, problems);
        }

        Map<Verdict, Object> triStates = new EnumMap<>(Verdict.class);
        if (!triStateType.isEnum()) {
            problems.add(names.triStateClass() + " is no longer an enum");
            return new Detection(Optional.empty(), false, problems);
        }
        for (Object constant : triStateType.getEnumConstants()) {
            try {
                triStates.put(Verdict.valueOf(((Enum<?>) constant).name()), constant);
            } catch (IllegalArgumentException ignored) {
                // A constant this mod does not know about; it is never produced.
            }
        }
        for (Verdict verdict : Verdict.values()) {
            if (!triStates.containsKey(verdict)) {
                problems.add(names.triStateClass() + " has no constant " + verdict);
            }
        }

        Method register;
        Method invoker;
        try {
            register = eventType.getMethod("register", Object.class);
            invoker = eventType.getMethod("invoker");
        } catch (NoSuchMethodException e) {
            problems.add(names.eventClass() + " lacks register(Object) or invoker(): " + e.getMessage());
            return new Detection(Optional.empty(), false, problems);
        }

        Map<Kind, Hook> hooks = new EnumMap<>(Kind.class);
        for (Kind kind : Kind.values()) {
            Class<?> type = kind == Kind.SOURCE_CHECK ? mainInterface : load(loader, names.apiPackage() + "." + kind.interfaceName);
            if (type == null) {
                problems.add(kind.interfaceName + " does not exist in this version of the API");
                continue;
            }
            List<String> shape = new ArrayList<>();
            Method method = checkShape(kind, type, names, sourceType, triStateType, eventType, shape);
            if (!shape.isEmpty()) {
                problems.addAll(shape);
                continue;
            }
            try {
                hooks.put(kind, new Hook(kind, type, type.getField("EVENT").get(null), method));
            } catch (ReflectiveOperationException | RuntimeException e) {
                problems.add(kind.interfaceName + ".EVENT cannot be read: " + e);
            }
        }

        if (!hooks.containsKey(Kind.SOURCE_CHECK)) {
            return new Detection(Optional.empty(), false, problems);
        }
        return new Detection(Optional.of(new FabricPermissionsApi(eventType, register, invoker, triStates, hooks)), false, problems);
    }

    /** Verifies one interface, adding to {@code problems} whatever differs. Returns its single method. */
    private static Method checkShape(Kind kind, Class<?> type, Names names, Class<?> sourceType,
                                     Class<?> triStateType, Class<?> eventType, List<String> problems) {
        String label = kind.interfaceName;
        if (!type.isInterface()) {
            problems.add(label + " is no longer an interface");
            return null;
        }
        try {
            Field field = type.getField("EVENT");
            if (!Modifier.isStatic(field.getModifiers()) || !eventType.isAssignableFrom(field.getType())) {
                problems.add(label + ".EVENT is not a static " + names.eventClass());
            }
        } catch (NoSuchFieldException e) {
            problems.add(label + " has no EVENT field");
        }

        List<Method> abstractMethods = Arrays.stream(type.getMethods())
                .filter(m -> Modifier.isAbstract(m.getModifiers()))
                .toList();
        if (abstractMethods.size() != 1) {
            problems.add(label + " declares " + abstractMethods.size() + " abstract methods, expected exactly 1");
            return null;
        }
        Method method = abstractMethods.getFirst();
        if (!method.getName().equals(kind.methodName)) {
            problems.add(label + " method is named " + method.getName() + ", expected " + kind.methodName);
        }
        Class<?> firstParameter = kind.online() ? sourceType : UUID.class;
        Class<?> expectedReturn = switch (kind) {
            case SOURCE_CHECK -> triStateType;
            case SOURCE_OPTION -> Optional.class;
            case OFFLINE_CHECK, OFFLINE_OPTION -> CompletableFuture.class;
        };
        Class<?>[] parameters = method.getParameterTypes();
        if (parameters.length != 2 || parameters[0] != firstParameter || parameters[1] != String.class) {
            problems.add(label + "." + method.getName() + " takes " + Arrays.toString(parameters) + ", expected ["
                    + firstParameter.getName() + ", java.lang.String]");
        }
        if (method.getReturnType() != expectedReturn) {
            problems.add(label + "." + method.getName() + " returns " + method.getReturnType().getName()
                    + ", expected " + expectedReturn.getName());
        }
        return method;
    }

    private static Class<?> load(ClassLoader loader, String name) {
        try {
            return Class.forName(name, false, loader);
        } catch (ClassNotFoundException | LinkageError e) {
            return null;
        }
    }

    /** Registers one proxy listener per usable event, all answering from {@code bridge}. */
    public List<Kind> register(PermissionBridge bridge) throws ReflectiveOperationException {
        for (Hook hook : hooks.values()) {
            Object listener = Proxy.newProxyInstance(hook.type().getClassLoader(), new Class<?>[]{hook.type()},
                    handler(hook.kind(), bridge));
            register.invoke(hook.event(), listener);
            registered.add(hook.kind());
        }
        return List.copyOf(registered);
    }

    public List<Kind> registered() {
        return List.copyOf(registered);
    }

    private InvocationHandler handler(Kind kind, PermissionBridge bridge) {
        return (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    default -> "iunacompat " + kind + " listener";
                };
            }
            if (method.isDefault()) {
                return InvocationHandler.invokeDefault(proxy, method, args);
            }
            return switch (kind) {
                case SOURCE_CHECK -> triStates.get(bridge.check(args[0], (String) args[1]));
                case SOURCE_OPTION -> bridge.option(args[0], (String) args[1]);
                case OFFLINE_CHECK -> bridge.checkOffline((UUID) args[0], (String) args[1]).thenApply(triStates::get);
                case OFFLINE_OPTION -> bridge.optionOffline((UUID) args[0], (String) args[1]);
            };
        };
    }

    /**
     * Asks the API the way a mod does ({@code Permissions.getPermissionValue(source, node)}):
     * through the event's invoker, so every registered listener takes part, not only this mod's.
     */
    public Verdict query(Object source, String node) throws ReflectiveOperationException {
        return toVerdict(call(Kind.SOURCE_CHECK, source, node));
    }

    /** The same for an offline UUID ({@code Permissions.getPermissionValue(uuid, node)}). */
    @SuppressWarnings("unchecked")
    public CompletableFuture<Verdict> queryOffline(UUID id, String node) throws ReflectiveOperationException {
        if (!hooks.containsKey(Kind.OFFLINE_CHECK)) {
            return CompletableFuture.completedFuture(Verdict.DEFAULT);
        }
        return ((CompletableFuture<Object>) call(Kind.OFFLINE_CHECK, id, node)).thenApply(this::toVerdict);
    }

    /** The same for an offline UUID's options ({@code Options.get(uuid, key)}). */
    @SuppressWarnings("unchecked")
    public CompletableFuture<Optional<String>> queryOfflineOption(UUID id, String key) throws ReflectiveOperationException {
        if (!hooks.containsKey(Kind.OFFLINE_OPTION)) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        return (CompletableFuture<Optional<String>>) call(Kind.OFFLINE_OPTION, id, key);
    }

    /** The same for options ({@code Options.get(source, key)}). */
    @SuppressWarnings("unchecked")
    public Optional<String> queryOption(Object source, String key) throws ReflectiveOperationException {
        if (!hooks.containsKey(Kind.SOURCE_OPTION)) {
            return Optional.empty();
        }
        return (Optional<String>) call(Kind.SOURCE_OPTION, source, key);
    }

    private Object call(Kind kind, Object first, String second) throws ReflectiveOperationException {
        Hook hook = hooks.get(kind);
        try {
            Object invokerInstance = invoker.invoke(hook.event());
            return hook.method().invoke(invokerInstance, first, second);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw e;
        }
    }

    private Verdict toVerdict(Object triState) {
        return Verdict.valueOf(((Enum<?>) triState).name());
    }

    /**
     * Whether the named class (a mod that consumes the API) resolves the very same
     * {@code PermissionCheckEvent} as this adapter, i.e. whether a listener registered here is the
     * one that mod's checks reach.
     *
     * @return empty if the class is not loaded (the mod is not installed)
     */
    public Optional<Boolean> sharesEventWith(String consumerClassName) {
        Class<?> ours = hooks.get(Kind.SOURCE_CHECK).type();
        try {
            Class<?> consumer = Class.forName(consumerClassName, false, ours.getClassLoader());
            Class<?> theirs = Class.forName(ours.getName(), false, consumer.getClassLoader());
            return Optional.of(theirs == ours);
        } catch (ClassNotFoundException | LinkageError e) {
            return Optional.empty();
        }
    }

    /** The event class used, for diagnostics. */
    public String eventClassName() {
        return eventType.getName();
    }

    /** Identity of the class loader that defined the main interface, for diagnostics. */
    public String interfaceLoader() {
        ClassLoader loader = hooks.get(Kind.SOURCE_CHECK).type().getClassLoader();
        return String.valueOf(loader);
    }
}
