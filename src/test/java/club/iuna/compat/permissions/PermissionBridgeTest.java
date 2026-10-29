package club.iuna.compat.permissions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionBridgeTest {
    private static final UUID PLAYER = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final Object PLAYER_SOURCE = new Object();
    private static final Object CONSOLE_SOURCE = new Object();

    private final FakeBackend backend = new FakeBackend();
    private final AtomicBoolean offline = new AtomicBoolean(true);
    private final AtomicBoolean options = new AtomicBoolean(true);
    private PermissionBridge bridge;

    @BeforeEach
    void setUp() {
        SubjectResolver resolver = source -> source == PLAYER_SOURCE ? Optional.of(new Subject(PLAYER, source)) : Optional.empty();
        bridge = new PermissionBridge(backend, resolver, new BridgeStats(), offline::get, options::get, () -> false);
    }

    @Test
    void playerIsAnsweredFromTheBackend() {
        backend.online.put("banhammer.duration.1d", Verdict.TRUE);
        backend.online.put("banhammer.can_ban_admins", Verdict.FALSE);

        assertEquals(Verdict.TRUE, bridge.check(PLAYER_SOURCE, "banhammer.duration.1d"));
        assertEquals(Verdict.FALSE, bridge.check(PLAYER_SOURCE, "banhammer.can_ban_admins"));
    }

    @Test
    void unsetNodeKeepsTheModsOwnDefault() {
        assertEquals(Verdict.DEFAULT, bridge.check(PLAYER_SOURCE, "easyauth.commands.login"));
    }

    @Test
    void consoleAndOtherSourcesGetNoOpinion() {
        backend.online.put("anything", Verdict.TRUE);

        assertEquals(Verdict.DEFAULT, bridge.check(CONSOLE_SOURCE, "anything"));
        assertEquals(Optional.empty(), bridge.option(CONSOLE_SOURCE, "prefix"));
    }

    @Test
    void optionsAreAnsweredForPlayersOnly() {
        backend.options.put("styled_chat.display_name", "Staff");

        assertEquals(Optional.of("Staff"), bridge.option(PLAYER_SOURCE, "styled_chat.display_name"));
        assertEquals(Optional.empty(), bridge.option(PLAYER_SOURCE, "styled_chat.missing"));
        assertEquals(1, bridge.stats().optionsAnswered());
        assertEquals(1, bridge.stats().optionsMissed());
    }

    @Test
    void optionsCanBeSwitchedOff() {
        backend.options.put("prefix", "[x]");
        options.set(false);

        assertEquals(Optional.empty(), bridge.option(PLAYER_SOURCE, "prefix"));
        assertEquals(Optional.empty(), bridge.optionOffline(PLAYER, "prefix").join());
    }

    @Test
    void offlineQueriesAreAnsweredFromStorage() {
        backend.offline.put("banhammer.block_punishments", Verdict.TRUE);
        backend.options.put("prefix", "[x]");

        assertEquals(Verdict.TRUE, bridge.checkOffline(PLAYER, "banhammer.block_punishments").join());
        assertEquals(Verdict.DEFAULT, bridge.checkOffline(PLAYER, "other").join());
        assertEquals(Optional.of("[x]"), bridge.optionOffline(PLAYER, "prefix").join());
    }

    @Test
    void offlineQueriesCanBeSwitchedOffWithoutTouchingStorage() {
        backend.offline.put("banhammer.block_punishments", Verdict.TRUE);
        offline.set(false);

        assertEquals(Verdict.DEFAULT, bridge.checkOffline(PLAYER, "banhammer.block_punishments").join());
        assertEquals(Optional.empty(), bridge.optionOffline(PLAYER, "prefix").join());
    }

    @Test
    void aFailingBackendNeverThrowsAndLeavesTheDefaultInForce() {
        backend.failure = new IllegalStateException("LuckPerms is not loaded");

        assertEquals(Verdict.DEFAULT, bridge.check(PLAYER_SOURCE, "a"));
        assertEquals(Optional.empty(), bridge.option(PLAYER_SOURCE, "b"));
        assertEquals(Verdict.DEFAULT, bridge.checkOffline(PLAYER, "c").join());
        assertEquals(Optional.empty(), bridge.optionOffline(PLAYER, "d").join());
        assertEquals(4, bridge.stats().errors());
        assertTrue(bridge.stats().lastError().contains("LuckPerms is not loaded"));
    }

    @Test
    void aBackendReturningNullIsTreatedAsNoOpinion() {
        PermissionBackend nulls = new FakeBackend() {
            @Override
            public CompletableFuture<Verdict> checkOffline(UUID id, String node) {
                return CompletableFuture.completedFuture(null);
            }
        };
        PermissionBridge nullBridge = new PermissionBridge(nulls, source -> Optional.empty(), new BridgeStats(), () -> true, () -> true, () -> false);

        assertEquals(Verdict.DEFAULT, nullBridge.checkOffline(PLAYER, "x").join());
    }

    @Test
    void everyAnswerIsCounted() {
        backend.online.put("yes", Verdict.TRUE);
        backend.online.put("no", Verdict.FALSE);

        bridge.check(PLAYER_SOURCE, "yes");
        bridge.check(PLAYER_SOURCE, "no");
        bridge.check(PLAYER_SOURCE, "unset");
        bridge.check(CONSOLE_SOURCE, "console");

        assertEquals(1, bridge.stats().allowed());
        assertEquals(1, bridge.stats().denied());
        assertEquals(2, bridge.stats().undecided());
    }
}
