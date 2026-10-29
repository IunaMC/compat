package club.iuna.compat.command;

import club.iuna.compat.IunaCompat;
import club.iuna.compat.module.CompatModule;
import club.iuna.compat.permissions.BridgeStats;
import club.iuna.compat.permissions.PermissionsModule;
import club.iuna.compat.permissions.Verdict;
import club.iuna.compat.permissions.fabric.FabricPermissionsApi;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * {@code /iunacompat}: a read-only diagnostic for administrators.
 *
 * <ul>
 *   <li>{@code status}: what each module is doing;</li>
 *   <li>{@code check <uuid> <node> [online]} and {@code option <uuid> <key> [online]}: ask the
 *       Fabric API the way a mod does, so the answer is what Banhammer, Styled Chat and the
 *       others would get. Without {@code online} the query is made about an offline UUID; with
 *       it, about the player (the real one if connected, otherwise a throwaway fake player).</li>
 * </ul>
 */
public final class CompatCommand {
    private static final int TIMEOUT_SECONDS = 10;

    private CompatCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("iunacompat")
                .requires(source -> source.hasPermission(4))
                .then(Commands.literal("status").executes(CompatCommand::status))
                .then(Commands.literal("check")
                        .then(Commands.argument("uuid", UuidArgument.uuid())
                                .then(Commands.argument("node", StringArgumentType.word())
                                        .executes(context -> query(context, false, false))
                                        .then(Commands.literal("online").executes(context -> query(context, false, true))))))
                .then(Commands.literal("option")
                        .then(Commands.argument("uuid", UuidArgument.uuid())
                                .then(Commands.argument("key", StringArgumentType.word())
                                        .executes(context -> query(context, true, false))
                                        .then(Commands.literal("online").executes(context -> query(context, true, true)))))));
    }

    private static int status(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        for (CompatModule module : IunaCompat.modules()) {
            source.sendSuccess(() -> Component.literal("[" + module.id() + "] " + module.status().state() + ": " + module.status().detail()), false);
        }
        PermissionsModule permissions = IunaCompat.permissions();
        permissions.api().ifPresent(api -> source.sendSuccess(() -> Component.literal(
                "[permissions] events " + api.registered() + " on " + api.eventClassName()), false));
        permissions.consumerReport().forEach(line -> source.sendSuccess(() -> Component.literal("[permissions] consumer " + line), false));
        permissions.problems().forEach(problem -> source.sendSuccess(() -> Component.literal("[permissions] problem: " + problem), false));
        permissions.bridge().map(bridge -> bridge.stats()).ifPresent(stats -> source.sendSuccess(() -> Component.literal(format(stats)), false));
        return 1;
    }

    private static String format(BridgeStats stats) {
        return "[permissions] answered: allow=" + stats.allowed() + " deny=" + stats.denied() + " no-opinion=" + stats.undecided()
                + " options=" + stats.optionsAnswered() + "/" + (stats.optionsAnswered() + stats.optionsMissed())
                + " errors=" + stats.errors() + (stats.lastError() == null ? "" : " (last: " + stats.lastError() + ")");
    }

    private static int query(CommandContext<CommandSourceStack> context, boolean option, boolean online) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        UUID id = UuidArgument.getUuid(context, "uuid");
        String name = StringArgumentType.getString(context, option ? "key" : "node");
        FabricPermissionsApi api = IunaCompat.permissions().api().orElseThrow(
                () -> new SimpleCommandExceptionType(Component.literal("The permissions bridge is not running (see /iunacompat status)")).create());
        try {
            String result;
            if (option) {
                result = optionResult(api, source, id, name, online);
            } else {
                result = checkResult(api, source, id, name, online);
            }
            source.sendSuccess(() -> Component.literal((option ? "option " : "check ") + id + " " + name + ": " + result), false);
            return 1;
        } catch (CommandSyntaxException e) {
            throw e;
        } catch (Exception e) {
            throw new SimpleCommandExceptionType(Component.literal("Query failed: " + e)).create();
        }
    }

    private static String checkResult(FabricPermissionsApi api, CommandSourceStack source, UUID id, String node, boolean online) throws Exception {
        // The offline query also loads the user into LuckPerms, which the online query below relies on.
        Verdict offline = api.queryOffline(id, node).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!online) {
            return "offline=" + offline;
        }
        Verdict onlineVerdict = api.query(playerFor(source, id).createCommandSourceStack(), node);
        return "offline=" + offline + " online=" + onlineVerdict;
    }

    private static String optionResult(FabricPermissionsApi api, CommandSourceStack source, UUID id, String key, boolean online) throws Exception {
        // An offline permission query makes LuckPerms load the user, which the online query needs
        // (the offline option event itself may fail inside the API, see below).
        api.queryOffline(id, "iunacompat.probe").get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        String offline;
        try {
            offline = api.queryOfflineOption(id, key).get(TIMEOUT_SECONDS, TimeUnit.SECONDS).orElse("<none>");
        } catch (ExecutionException e) {
            // fabric-permissions-api up to 0.3.3 throws a NullPointerException here whenever any
            // listener is registered (the combiner starts from completedFuture(null)); fixed in 0.4.1.
            offline = "<failed inside the API: " + e.getCause() + ">";
        }
        if (!online) {
            return "offline=" + offline;
        }
        Optional<String> value = api.queryOption(playerFor(source, id).createCommandSourceStack(), key);
        return "offline=" + offline + " online=" + value.orElse("<none>");
    }

    private static ServerPlayer playerFor(CommandSourceStack source, UUID id) {
        ServerPlayer connected = source.getServer().getPlayerList().getPlayer(id);
        if (connected != null) {
            return connected;
        }
        return FakePlayerFactory.get(source.getServer().overworld(), new GameProfile(id, "iunacompat-probe"));
    }
}
