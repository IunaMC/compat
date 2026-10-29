package club.iuna.compat.permissions;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.util.Tristate;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Answers from LuckPerms through its public API. This is the only class that touches
 * {@code net.luckperms.api}, and it is only loaded when LuckPerms is installed.
 *
 * <p>Online players are answered from the user LuckPerms already holds in memory, with the
 * contexts of the player's current state (world, gamemode, other mods' contexts), exactly like
 * LuckPerms' own Fabric listener. A user that is not loaded yet (a player still logging in, or a
 * fake player) gets no opinion instead of a blocking storage read on the server thread.
 */
public final class LuckPermsBackend implements PermissionBackend {
    /** Group whose cached data is used to introduce nodes to the permission registry. */
    static final String REGISTRATION_GROUP = "default";

    private final Supplier<LuckPerms> api;

    public LuckPermsBackend() {
        this(LuckPermsProvider::get);
    }

    /** For tests: lets the API be replaced. */
    LuckPermsBackend(Supplier<LuckPerms> api) {
        this.api = api;
    }

    @Override
    public Verdict check(Subject subject, String node) {
        LuckPerms luckPerms = api.get();
        User user = luckPerms.getUserManager().getUser(subject.id());
        if (user == null) {
            return Verdict.DEFAULT;
        }
        QueryOptions options = luckPerms.getContextManager().getQueryOptions(subject.handle());
        return verdict(user.getCachedData().getPermissionData(options).checkPermission(node));
    }

    @Override
    public Optional<String> option(Subject subject, String key) {
        LuckPerms luckPerms = api.get();
        User user = luckPerms.getUserManager().getUser(subject.id());
        if (user == null) {
            return Optional.empty();
        }
        QueryOptions options = luckPerms.getContextManager().getQueryOptions(subject.handle());
        return option(user.getCachedData().getMetaData(options), key);
    }

    @Override
    public CompletableFuture<Verdict> checkOffline(UUID id, String node) {
        return lookup(id).thenApply(user -> verdict(user.getCachedData().getPermissionData().checkPermission(node)));
    }

    @Override
    public CompletableFuture<Optional<String>> optionOffline(UUID id, String key) {
        return lookup(id).thenApply(user -> option(user.getCachedData().getMetaData(), key));
    }

    @Override
    public void registerNodes(Collection<String> nodes) {
        Group group = api.get().getGroupManager().getGroup(REGISTRATION_GROUP);
        if (group == null) {
            return;
        }
        // LuckPerms offers every node it is asked about to its permission registry, whatever the
        // answer is, so a plain query is enough to make the node appear in the editor.
        var data = group.getCachedData().getPermissionData();
        for (String node : nodes) {
            data.checkPermission(node);
        }
    }

    private CompletableFuture<User> lookup(UUID id) {
        LuckPerms luckPerms = api.get();
        User user = luckPerms.getUserManager().getUser(id);
        return user != null ? CompletableFuture.completedFuture(user) : luckPerms.getUserManager().loadUser(id);
    }

    /** Mirrors {@code getMetaOrChatMetaValue} of LuckPerms: "prefix" and "suffix" are chat meta. */
    static Optional<String> option(CachedMetaData meta, String key) {
        return Optional.ofNullable(switch (key) {
            case "prefix" -> meta.getPrefix();
            case "suffix" -> meta.getSuffix();
            default -> meta.getMetaValue(key);
        });
    }

    static Verdict verdict(Tristate state) {
        return switch (state) {
            case TRUE -> Verdict.TRUE;
            case FALSE -> Verdict.FALSE;
            case UNDEFINED -> Verdict.DEFAULT;
        };
    }
}
