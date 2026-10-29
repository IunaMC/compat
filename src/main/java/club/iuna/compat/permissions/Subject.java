package club.iuna.compat.permissions;

import java.util.UUID;

/**
 * An online player a permission query is about.
 *
 * @param id     the player's UUID
 * @param handle the platform object (a {@code ServerPlayer}); the backend hands it to LuckPerms
 *               to obtain the player's current contexts
 */
public record Subject(UUID id, Object handle) {
}
