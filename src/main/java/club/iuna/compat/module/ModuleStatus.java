package club.iuna.compat.module;

/**
 * State of a compatibility module after startup.
 *
 * @param state  what happened
 * @param detail one human-readable line, shown in the log and by {@code /iunacompat status}
 */
public record ModuleStatus(State state, String detail) {

    public enum State {
        /** Not started yet. */
        PENDING,
        /** Running. */
        ENABLED,
        /** Running, but something it wanted is missing or different (see the detail). */
        DEGRADED,
        /** Turned off in the config. */
        DISABLED,
        /** Nothing to do here: the mods it bridges are not installed. */
        UNAVAILABLE,
        /** Could not start; the pack keeps working as if the module did not exist. */
        FAILED
    }

    public static ModuleStatus pending() {
        return new ModuleStatus(State.PENDING, "not started");
    }
}
