package club.iuna.compat.permissions;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedDataManager;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.context.ContextManager;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.group.GroupManager;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.util.Tristate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LuckPermsBackendTest {
    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final Object PLAYER_HANDLE = new Object();

    private final LuckPerms luckPerms = mock(LuckPerms.class);
    private final UserManager users = mock(UserManager.class);
    private final GroupManager groups = mock(GroupManager.class);
    private final ContextManager contexts = mock(ContextManager.class);
    private final User user = mock(User.class);
    private final CachedDataManager data = mock(CachedDataManager.class);
    private final CachedPermissionData permissions = mock(CachedPermissionData.class);
    private final CachedMetaData meta = mock(CachedMetaData.class);
    private final QueryOptions options = mock(QueryOptions.class);
    private LuckPermsBackend backend;

    @BeforeEach
    void setUp() {
        when(luckPerms.getUserManager()).thenReturn(users);
        when(luckPerms.getGroupManager()).thenReturn(groups);
        when(luckPerms.getContextManager()).thenReturn(contexts);
        when(contexts.getQueryOptions(PLAYER_HANDLE)).thenReturn(options);
        when(user.getCachedData()).thenReturn(data);
        when(data.getPermissionData(options)).thenReturn(permissions);
        when(data.getMetaData(options)).thenReturn(meta);
        when(data.getPermissionData()).thenReturn(permissions);
        when(data.getMetaData()).thenReturn(meta);
        backend = new LuckPermsBackend(() -> luckPerms);
    }

    @Test
    void anOnlinePlayerIsAnsweredWithItsOwnContexts() {
        when(users.getUser(ID)).thenReturn(user);
        when(permissions.checkPermission("a")).thenReturn(Tristate.TRUE);
        when(permissions.checkPermission("b")).thenReturn(Tristate.FALSE);
        when(permissions.checkPermission("c")).thenReturn(Tristate.UNDEFINED);
        Subject subject = new Subject(ID, PLAYER_HANDLE);

        assertEquals(Verdict.TRUE, backend.check(subject, "a"));
        assertEquals(Verdict.FALSE, backend.check(subject, "b"));
        assertEquals(Verdict.DEFAULT, backend.check(subject, "c"));
        verify(data, atLeastOnce()).getPermissionData(options);
    }

    @Test
    void aPlayerWhoseUserIsNotLoadedGetsNoOpinionInsteadOfAStorageRead() {
        when(users.getUser(ID)).thenReturn(null);
        Subject subject = new Subject(ID, PLAYER_HANDLE);

        assertEquals(Verdict.DEFAULT, backend.check(subject, "a"));
        assertEquals(Optional.empty(), backend.option(subject, "prefix"));
        verify(users, never()).loadUser(any(UUID.class));
    }

    @Test
    void prefixAndSuffixAreChatMetaAndEverythingElseIsMeta() {
        when(users.getUser(ID)).thenReturn(user);
        when(meta.getPrefix()).thenReturn("[Staff]");
        when(meta.getSuffix()).thenReturn(null);
        when(meta.getMetaValue("styled_chat.display_name")).thenReturn("Name");
        Subject subject = new Subject(ID, PLAYER_HANDLE);

        assertEquals(Optional.of("[Staff]"), backend.option(subject, "prefix"));
        assertEquals(Optional.empty(), backend.option(subject, "suffix"));
        assertEquals(Optional.of("Name"), backend.option(subject, "styled_chat.display_name"));
        assertEquals(Optional.empty(), backend.option(subject, "unknown"));
    }

    @Test
    void anOfflineQueryUsesTheLoadedUserWithoutTouchingStorage() {
        when(users.getUser(ID)).thenReturn(user);
        when(permissions.checkPermission("banhammer.block_punishments")).thenReturn(Tristate.TRUE);

        assertEquals(Verdict.TRUE, backend.checkOffline(ID, "banhammer.block_punishments").join());
        verify(users, never()).loadUser(any(UUID.class));
    }

    @Test
    void anOfflineQueryLoadsTheUserWhenNeeded() {
        when(users.getUser(ID)).thenReturn(null);
        when(users.loadUser(ID)).thenReturn(CompletableFuture.completedFuture(user));
        when(permissions.checkPermission("x")).thenReturn(Tristate.FALSE);
        when(meta.getMetaValue("k")).thenReturn("v");

        assertEquals(Verdict.FALSE, backend.checkOffline(ID, "x").join());
        assertEquals(Optional.of("v"), backend.optionOffline(ID, "k").join());
    }

    @Test
    void knownNodesAreIntroducedThroughTheDefaultGroup() {
        Group group = mock(Group.class);
        CachedDataManager groupData = mock(CachedDataManager.class);
        CachedPermissionData groupPermissions = mock(CachedPermissionData.class);
        when(groups.getGroup("default")).thenReturn(group);
        when(group.getCachedData()).thenReturn(groupData);
        when(groupData.getPermissionData()).thenReturn(groupPermissions);

        backend.registerNodes(List.of("a.b", "c.d"));

        verify(groupPermissions).checkPermission("a.b");
        verify(groupPermissions).checkPermission("c.d");
    }

    @Test
    void registeringNodesWithoutTheDefaultGroupDoesNothing() {
        when(groups.getGroup("default")).thenReturn(null);

        backend.registerNodes(List.of("a.b"));

        verify(groups).getGroup("default");
    }
}
