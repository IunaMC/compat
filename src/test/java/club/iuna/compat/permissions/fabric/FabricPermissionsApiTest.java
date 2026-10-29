package club.iuna.compat.permissions.fabric;

import club.iuna.compat.permissions.BridgeStats;
import club.iuna.compat.permissions.PermissionBridge;
import club.iuna.compat.permissions.Subject;
import club.iuna.compat.permissions.Verdict;
import club.iuna.compat.permissions.fabric.FabricPermissionsApi.Detection;
import club.iuna.compat.permissions.fabric.FabricPermissionsApi.Kind;
import club.iuna.compat.permissions.fabric.FabricPermissionsApi.Names;
import club.iuna.compat.permissions.fabric.fakeapi.FakeSource;
import club.iuna.compat.permissions.fabric.fakeapi.TriState;
import club.iuna.compat.permissions.fabric.fakeapi.ok.OfflineOptionRequestEvent;
import club.iuna.compat.permissions.fabric.fakeapi.ok.OfflinePermissionCheckEvent;
import club.iuna.compat.permissions.fabric.fakeapi.ok.OptionRequestEvent;
import club.iuna.compat.permissions.fabric.fakeapi.ok.PermissionCheckEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs the adapter against test doubles that have the shape of fabric-permissions-api, and against
 * deliberately broken variants of it. The real API is covered by {@link FabricContractTest}.
 */
class FabricPermissionsApiTest {
    private static final String BASE = "club.iuna.compat.permissions.fabric.fakeapi";
    private static final UUID PLAYER = UUID.fromString("00000000-0000-0000-0000-000000000003");

    private final FakeSource playerSource = new FakeSource() {
    };
    private final FakeSource consoleSource = new FakeSource() {
    };
    private final Map<String, Verdict> online = new HashMap<>();
    private final Map<String, Verdict> offline = new HashMap<>();
    private final Map<String, String> options = new HashMap<>();
    private PermissionBridge bridge;

    private static Detection detect(String variant) {
        return FabricPermissionsApi.detect(FabricPermissionsApiTest.class.getClassLoader(),
                new Names(BASE + "." + variant, BASE + ".Event", BASE + ".TriState"), FakeSource.class);
    }

    @BeforeEach
    void setUp() {
        bridge = new PermissionBridge(new club.iuna.compat.permissions.PermissionBackend() {
            @Override
            public Verdict check(Subject subject, String node) {
                return online.getOrDefault(node, Verdict.DEFAULT);
            }

            @Override
            public Optional<String> option(Subject subject, String key) {
                return Optional.ofNullable(options.get(key));
            }

            @Override
            public CompletableFuture<Verdict> checkOffline(UUID id, String node) {
                return CompletableFuture.completedFuture(offline.getOrDefault(node, Verdict.DEFAULT));
            }

            @Override
            public CompletableFuture<Optional<String>> optionOffline(UUID id, String key) {
                return CompletableFuture.completedFuture(Optional.ofNullable(options.get(key)));
            }

            @Override
            public void registerNodes(java.util.Collection<String> nodes) {
            }
        }, source -> source == playerSource ? Optional.of(new Subject(PLAYER, source)) : Optional.empty(),
                new BridgeStats(), () -> true, () -> true, () -> false);
    }

    @Test
    void anApiThatIsNotThereIsAbsentNotBroken() {
        Detection detection = FabricPermissionsApi.detect(getClass().getClassLoader(),
                new Names("does.not.exist", BASE + ".Event", BASE + ".TriState"), FakeSource.class);

        assertTrue(detection.absent());
        assertTrue(detection.api().isEmpty());
        assertTrue(detection.problems().isEmpty());
    }

    @Test
    void anApiWithTheExpectedShapeIsAcceptedWithoutProblems() {
        Detection detection = detect("ok");

        assertFalse(detection.absent());
        assertTrue(detection.api().isPresent());
        assertEquals(List.of(), detection.problems());
    }

    @Test
    void aRenamedMethodIsReportedAndNothingIsRegistered() {
        Detection detection = detect("renamed");

        assertTrue(detection.api().isEmpty());
        assertTrue(detection.problems().stream().anyMatch(p -> p.contains("check") && p.contains("onPermissionCheck")), detection.problems().toString());
    }

    @Test
    void aChangedReturnTypeIsReported() {
        Detection detection = detect("wrongreturn");

        assertTrue(detection.api().isEmpty());
        assertTrue(detection.problems().stream().anyMatch(p -> p.contains("returns java.lang.Boolean")), detection.problems().toString());
    }

    @Test
    void anOlderApiWithoutOfflineEventsStillWorksForTheOnlineOnes() throws Exception {
        Detection detection = detect("partial");

        assertEquals(2, detection.problems().size(), detection.problems().toString());
        FabricPermissionsApi api = detection.api().orElseThrow();
        assertEquals(List.of(Kind.SOURCE_CHECK, Kind.SOURCE_OPTION), List.copyOf(api.register(bridge)));
        assertEquals(Verdict.DEFAULT, api.queryOffline(PLAYER, "x").join());
    }

    @Test
    void anEventWhoseShapeChangedIsSkippedWhileTheOthersKeepWorking() throws Exception {
        Detection detection = detect("extramethod");

        assertTrue(detection.problems().stream().anyMatch(p -> p.contains("OptionRequestEvent") && p.contains("2 abstract methods")), detection.problems().toString());
        FabricPermissionsApi api = detection.api().orElseThrow();
        assertEquals(List.of(Kind.SOURCE_CHECK, Kind.OFFLINE_CHECK, Kind.OFFLINE_OPTION), List.copyOf(api.register(bridge)));
    }

    @Test
    void thePlayerIsAnsweredThroughTheFabricEvent() throws Exception {
        online.put("banhammer.duration.1d", Verdict.TRUE);
        online.put("banhammer.can_ban_admins", Verdict.FALSE);
        FabricPermissionsApi api = detect("ok").api().orElseThrow();
        api.register(bridge);

        // Called like Permissions.getPermissionValue(source, node): through the event's invoker.
        assertEquals(TriState.TRUE, PermissionCheckEvent.EVENT.invoker().onPermissionCheck(playerSource, "banhammer.duration.1d"));
        assertEquals(TriState.FALSE, PermissionCheckEvent.EVENT.invoker().onPermissionCheck(playerSource, "banhammer.can_ban_admins"));
        assertEquals(TriState.DEFAULT, PermissionCheckEvent.EVENT.invoker().onPermissionCheck(playerSource, "easyauth.commands.login"));
        assertEquals(TriState.DEFAULT, PermissionCheckEvent.EVENT.invoker().onPermissionCheck(consoleSource, "banhammer.duration.1d"));
        // And through the adapter's own probe, which the diagnostic command uses.
        assertEquals(Verdict.TRUE, api.query(playerSource, "banhammer.duration.1d"));
    }

    @Test
    void optionsAreAnsweredThroughTheFabricEvent() throws Exception {
        options.put("styled_chat.display_name", "Staff");
        FabricPermissionsApi api = detect("ok").api().orElseThrow();
        api.register(bridge);

        assertEquals(Optional.of("Staff"), OptionRequestEvent.EVENT.invoker().onOptionRequest(playerSource, "styled_chat.display_name"));
        assertEquals(Optional.empty(), OptionRequestEvent.EVENT.invoker().onOptionRequest(consoleSource, "styled_chat.display_name"));
        assertEquals(Optional.of("Staff"), api.queryOption(playerSource, "styled_chat.display_name"));
    }

    @Test
    void offlineQueriesAreAnsweredThroughTheFabricEvents() throws Exception {
        offline.put("banhammer.block_punishments", Verdict.TRUE);
        options.put("prefix", "[x]");
        FabricPermissionsApi api = detect("ok").api().orElseThrow();
        api.register(bridge);

        assertEquals(TriState.TRUE, OfflinePermissionCheckEvent.EVENT.invoker().onPermissionCheck(PLAYER, "banhammer.block_punishments").join());
        assertEquals(TriState.DEFAULT, OfflinePermissionCheckEvent.EVENT.invoker().onPermissionCheck(PLAYER, "other").join());
        assertEquals(Optional.of("[x]"), OfflineOptionRequestEvent.EVENT.invoker().onOptionRequest(PLAYER, "prefix").join());
        assertEquals(Verdict.TRUE, api.queryOffline(PLAYER, "banhammer.block_punishments").join());
    }

    @Test
    void ourListenerDefersToOthersWhenItHasNoOpinionAndIsOverriddenByEarlierOnes() throws Exception {
        online.put("ours", Verdict.TRUE);
        FabricPermissionsApi api = detect("ok").api().orElseThrow();
        // Another listener registered before ours wins when it decides, and is consulted when we do not.
        PermissionCheckEvent.EVENT.register((source, node) -> node.equals("theirs") ? TriState.FALSE : TriState.DEFAULT);
        api.register(bridge);

        assertEquals(TriState.FALSE, PermissionCheckEvent.EVENT.invoker().onPermissionCheck(playerSource, "theirs"));
        assertEquals(TriState.TRUE, PermissionCheckEvent.EVENT.invoker().onPermissionCheck(playerSource, "ours"));
    }

    @Test
    void theRegisteredListenersAreWellBehavedObjects() throws Exception {
        FabricPermissionsApi api = detect("ok").api().orElseThrow();
        api.register(bridge);

        assertEquals(4, api.registered().size());
        assertTrue(api.eventClassName().endsWith(".Event"));
    }
}
