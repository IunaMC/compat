package club.iuna.compat;

import net.neoforged.neoforge.common.ModConfigSpec;

/** {@code config/iunacompat-common.toml}. Every default keeps the module on. */
public final class CompatConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue PERMISSIONS_ENABLED;
    public static final ModConfigSpec.BooleanValue PERMISSIONS_OPTIONS;
    public static final ModConfigSpec.BooleanValue PERMISSIONS_OFFLINE;
    public static final ModConfigSpec.BooleanValue PERMISSIONS_REGISTER_KNOWN_NODES;
    public static final ModConfigSpec.BooleanValue DEBUG_LOG_CHECKS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Bridge between fabric-permissions-api and LuckPerms.",
                        "Needs LuckPerms and at least one Fabric mod (running through Sinytra Connector) that embeds fabric-permissions-api.")
                .push("permissions");
        PERMISSIONS_ENABLED = builder
                .comment("Master switch. Off: the Fabric mods keep ignoring LuckPerms, as without this mod.")
                .define("enabled", true);
        PERMISSIONS_OPTIONS = builder
                .comment("Also answer options (LuckPerms meta, prefix and suffix), e.g. the styled_chat.* keys used by Styled Chat.")
                .define("options", true);
        PERMISSIONS_OFFLINE = builder
                .comment("Also answer queries about players that are not online (loads the user from LuckPerms storage).",
                        "Banhammer uses this to check banhammer.block_punishments before punishing an offline player.")
                .define("offline", true);
        PERMISSIONS_REGISTER_KNOWN_NODES = builder
                .comment("At server start, tell LuckPerms about the known nodes of the pack's Fabric mods so they show up in tab completion and in the web editor.",
                        "Grants nothing.")
                .define("registerKnownNodes", true);
        builder.pop();

        builder.push("debug");
        DEBUG_LOG_CHECKS = builder
                .comment("Log every permission and option query at INFO level. Very noisy: use it for a minute, not permanently.")
                .define("logChecks", false);
        builder.pop();

        SPEC = builder.build();
    }

    private CompatConfig() {
    }
}
