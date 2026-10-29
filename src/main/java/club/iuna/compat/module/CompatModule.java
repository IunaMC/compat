package club.iuna.compat.module;

/**
 * One independent compatibility fix. Modules never depend on each other, and a module that cannot
 * start must leave the server exactly as if it did not exist.
 */
public interface CompatModule {

    /** Short stable identifier, also the name of the module's section in the config. */
    String id();

    /** What the module fixes, in one line. */
    String description();

    /** Starts the module once. Must not throw: report trouble through the returned status. */
    ModuleStatus enable();

    /** The status {@link #enable()} returned (or {@link ModuleStatus#pending()}). */
    ModuleStatus status();
}
