package club.iuna.compat.permissions.fabric;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * The early warning for a breaking change in {@code fabric-permissions-api}.
 *
 * <p>The adapter reads the API by reflection, so a change in it would not break the build. This
 * test opens the real published jars (the versions in the {@code fabricPermissionsApiJars} system
 * property, set by Gradle) and asserts the exact shape {@link FabricPermissionsApi} relies on:
 * four interfaces, each with one abstract method, taking {@code (source or UUID, String)}, plus a
 * static {@code EVENT} field. The jars are mapped to intermediary names, so the command source is
 * matched by its intermediary name as well as by its Mojang and Yarn names.
 *
 * <p>The pack's version is checked by every build. The scheduled workflow also checks the newest
 * published version, so a break shows up before the pack updates to it.
 */
class FabricContractTest {
    private static final String PACKAGE = "me/lucko/fabric/api/permissions/v0/";
    private static final Set<String> SOURCE_TYPES = Set.of(
            "net/minecraft/class_2172",
            "net/minecraft/commands/SharedSuggestionProvider",
            "net/minecraft/command/CommandSource");
    private static final String TRI_STATE = "net/fabricmc/fabric/api/util/TriState";
    private static final String EVENT = "net/fabricmc/fabric/api/event/Event";

    private record Expectation(String type, String method, boolean online, String returnType) {
    }

    private static final List<Expectation> EXPECTATIONS = List.of(
            new Expectation("PermissionCheckEvent", "onPermissionCheck", true, TRI_STATE),
            new Expectation("OptionRequestEvent", "onOptionRequest", true, "java/util/Optional"),
            new Expectation("OfflinePermissionCheckEvent", "onPermissionCheck", false, "java/util/concurrent/CompletableFuture"),
            new Expectation("OfflineOptionRequestEvent", "onOptionRequest", false, "java/util/concurrent/CompletableFuture"));

    @TestFactory
    Stream<DynamicTest> publishedApiKeepsTheShapeTheAdapterRelaysOn() {
        String property = System.getProperty("fabricPermissionsApiJars", "");
        assertTrue(!property.isBlank(), "fabricPermissionsApiJars is not set; run the tests through Gradle");
        List<DynamicTest> tests = new ArrayList<>();
        for (String entry : property.split(",")) {
            String[] parts = entry.split("=", 2);
            String version = parts[0];
            Path jar = Path.of(parts[1]);
            for (Expectation expectation : EXPECTATIONS) {
                tests.add(dynamicTest(version + " " + expectation.type(), () -> verify(jar, expectation)));
            }
        }
        return tests.stream();
    }

    private static void verify(Path jar, Expectation expectation) throws IOException {
        ClassNode node = read(jar, PACKAGE + expectation.type() + ".class");
        assertNotNull(node, expectation.type() + " is missing from " + jar.getFileName());
        assertTrue((node.access & Opcodes.ACC_INTERFACE) != 0, expectation.type() + " is not an interface");

        List<MethodNode> abstractMethods = node.methods.stream()
                .filter(m -> (m.access & Opcodes.ACC_ABSTRACT) != 0)
                .toList();
        assertEquals(1, abstractMethods.size(), expectation.type() + " abstract methods: " + abstractMethods.stream().map(m -> m.name + m.desc).toList());
        MethodNode method = abstractMethods.getFirst();
        assertEquals(expectation.method(), method.name, expectation.type() + " method name");

        Type[] arguments = Type.getArgumentTypes(method.desc);
        assertEquals(2, arguments.length, expectation.type() + " parameters: " + method.desc);
        if (expectation.online()) {
            assertTrue(SOURCE_TYPES.contains(arguments[0].getInternalName()), expectation.type() + " first parameter: " + arguments[0]);
        } else {
            assertEquals("java/util/UUID", arguments[0].getInternalName(), expectation.type() + " first parameter");
        }
        assertEquals("java/lang/String", arguments[1].getInternalName(), expectation.type() + " second parameter");
        assertEquals(expectation.returnType(), Type.getReturnType(method.desc).getInternalName(), expectation.type() + " return type");

        var event = node.fields.stream().filter(f -> f.name.equals("EVENT")).findFirst().orElse(null);
        assertNotNull(event, expectation.type() + ".EVENT is missing");
        assertTrue((event.access & Opcodes.ACC_STATIC) != 0, expectation.type() + ".EVENT is not static");
        assertEquals("L" + EVENT + ";", event.desc, expectation.type() + ".EVENT type");
    }

    private static ClassNode read(Path jar, String entryName) throws IOException {
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            ZipEntry entry = zip.getEntry(entryName);
            if (entry == null) {
                return null;
            }
            try (InputStream in = zip.getInputStream(entry)) {
                ClassNode node = new ClassNode();
                new ClassReader(in.readAllBytes()).accept(node, ClassReader.SKIP_CODE);
                return node;
            }
        }
    }
}
