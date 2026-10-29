package club.iuna.compat.permissions;

import club.iuna.compat.CompatConfig;
import club.iuna.compat.module.CompatModule;
import club.iuna.compat.module.ModuleStatus;
import club.iuna.compat.module.ModuleStatus.State;
import club.iuna.compat.permissions.fabric.FabricPermissionsApi;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.SharedSuggestionProvider;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Routes {@code fabric-permissions-api} checks to LuckPerms.
 *
 * <p>Why it exists: LuckPerms for NeoForge answers NeoForge's {@code PermissionAPI} and rewrites
 * command requirements to {@code command.*} nodes, but it never listens to the Fabric API's
 * events. Fabric mods that run through Sinytra Connector (Banhammer, Styled Chat, Styled Player
 * List, Essential Commands, EasyAuth) ask those events, get "no opinion" from everyone and fall
 * back to their built-in defaults, so nodes like {@code banhammer.duration.1d} are never read.
 */
public final class PermissionsModule implements CompatModule {
    private static final Logger LOGGER = LogUtils.getLogger();
    static final String LUCKPERMS_MOD_ID = "luckperms";

    private volatile ModuleStatus status = ModuleStatus.pending();
    private volatile FabricPermissionsApi api;
    private volatile PermissionBridge bridge;
    private volatile PermissionBackend backend;
    private volatile List<String> problems = List.of();

    @Override
    public String id() {
        return "permissions";
    }

    @Override
    public String description() {
        return "Routes fabric-permissions-api permission and option checks to LuckPerms";
    }

    @Override
    public ModuleStatus enable() {
        status = start();
        LOGGER.info("[{}] {}: {}", id(), status.state(), status.detail());
        return status;
    }

    private ModuleStatus start() {
        if (!CompatConfig.PERMISSIONS_ENABLED.get()) {
            return new ModuleStatus(State.DISABLED, "turned off in the config (permissions.enabled = false)");
        }
        if (!ModList.get().isLoaded(LUCKPERMS_MOD_ID)) {
            return new ModuleStatus(State.UNAVAILABLE, "LuckPerms is not installed");
        }
        try {
            FabricPermissionsApi.Detection detection = FabricPermissionsApi.detect(
                    PermissionsModule.class.getClassLoader(), FabricPermissionsApi.Names.FABRIC, SharedSuggestionProvider.class);
            problems = detection.problems();
            problems.forEach(problem -> LOGGER.warn("[{}] {}", id(), problem));
            if (detection.absent()) {
                return new ModuleStatus(State.UNAVAILABLE, "fabric-permissions-api is not loaded (no Fabric mod embeds it)");
            }
            Optional<FabricPermissionsApi> found = detection.api();
            if (found.isEmpty()) {
                return new ModuleStatus(State.FAILED, "the API changed in a way this version does not handle: " + problems.getFirst());
            }

            PermissionBackend newBackend = new LuckPermsBackend();
            PermissionBridge newBridge = new PermissionBridge(newBackend, new MinecraftSubjectResolver(), new BridgeStats(),
                    CompatConfig.PERMISSIONS_OFFLINE::get, CompatConfig.PERMISSIONS_OPTIONS::get, CompatConfig.DEBUG_LOG_CHECKS::get);
            List<FabricPermissionsApi.Kind> registered = found.get().register(newBridge);
            api = found.get();
            bridge = newBridge;
            backend = newBackend;
            String detail = "listening on " + registered.size() + " of 4 events " + registered;
            return new ModuleStatus(problems.isEmpty() ? State.ENABLED : State.DEGRADED, detail);
        } catch (Throwable error) {
            LOGGER.error("[{}] could not start", id(), error);
            return new ModuleStatus(State.FAILED, error.toString());
        }
    }

    /** Mods of the pack known to consume the API, by one class that calls it. */
    private static final Map<String, String> CONSUMERS = new LinkedHashMap<>();

    static {
        CONSUMERS.put("Banhammer", "eu.pb4.banhammer.impl.config.ConfigManager");
        CONSUMERS.put("Styled Chat", "eu.pb4.styledchat.StyledChatUtils");
        CONSUMERS.put("Styled Player List", "eu.pb4.styledplayerlist.command.Commands");
        CONSUMERS.put("Essential Commands", "com.fibermc.essentialcommands.ECPerms");
        CONSUMERS.put("EasyAuth", "xyz.nikitacartes.easyauth.integrations.Permissions");
    }

    /** One line per known consumer: whether it is installed and reaches the event this module listens on. */
    public List<String> consumerReport() {
        FabricPermissionsApi current = api;
        List<String> lines = new ArrayList<>();
        if (current == null) {
            return lines;
        }
        CONSUMERS.forEach((label, className) -> lines.add(label + ": " + current.sharesEventWith(className)
                .map(shared -> shared ? "uses the same event (bridged)" : "uses ANOTHER copy of the event (NOT bridged)")
                .orElse("not installed")));
        return lines;
    }

    /** Introduces the known nodes to LuckPerms. Call once LuckPerms is running (server started). */
    public void registerKnownNodes() {
        PermissionBackend current = backend;
        if (current == null || !CompatConfig.PERMISSIONS_REGISTER_KNOWN_NODES.get()) {
            return;
        }
        try {
            List<String> nodes = NodeCatalog.load();
            current.registerNodes(nodes);
            LOGGER.info("[{}] registered {} known nodes with LuckPerms", id(), nodes.size());
        } catch (Throwable error) {
            LOGGER.warn("[{}] could not register the known nodes (harmless): {}", id(), error.toString());
        }
    }

    @Override
    public ModuleStatus status() {
        return status;
    }

    public Optional<FabricPermissionsApi> api() {
        return Optional.ofNullable(api);
    }

    public Optional<PermissionBridge> bridge() {
        return Optional.ofNullable(bridge);
    }

    public List<String> problems() {
        return problems;
    }
}
