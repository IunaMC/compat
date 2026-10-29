package club.iuna.compat.permissions;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** The permission store the bridge answers from. Implementations must not throw for unknown users. */
public interface PermissionBackend {

    /** Permission of an online player, resolved with the player's current contexts. */
    Verdict check(Subject subject, String node);

    /** Option (meta value, prefix or suffix) of an online player. */
    Optional<String> option(Subject subject, String key);

    /** Permission of a player that may be offline; loads the user from storage if needed. */
    CompletableFuture<Verdict> checkOffline(UUID id, String node);

    /** Option of a player that may be offline; loads the user from storage if needed. */
    CompletableFuture<Optional<String>> optionOffline(UUID id, String key);

    /** Makes nodes known to the editor and to tab completion, without granting anything. */
    void registerNodes(Collection<String> nodes);
}
