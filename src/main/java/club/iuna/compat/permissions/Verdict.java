package club.iuna.compat.permissions;

/**
 * Result of a permission query, with the same three states and the same constant names as
 * {@code net.fabricmc.fabric.api.util.TriState}. {@link #DEFAULT} means "no opinion": the caller
 * falls back to its own default (a boolean or an operator level).
 */
public enum Verdict {
    TRUE,
    FALSE,
    DEFAULT
}
