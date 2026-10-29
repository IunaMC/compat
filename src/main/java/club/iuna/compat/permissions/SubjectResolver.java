package club.iuna.compat.permissions;

import java.util.Optional;

/** Turns the {@code source} of a Fabric permission query into an online player, if it is one. */
@FunctionalInterface
public interface SubjectResolver {
    /**
     * @param source the object the Fabric API passed (a {@code CommandSourceStack} in practice)
     * @return the player behind the source; empty for the console, command blocks, RCON and so on
     */
    Optional<Subject> resolve(Object source);
}
