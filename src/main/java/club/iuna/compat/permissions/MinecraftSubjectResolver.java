package club.iuna.compat.permissions;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/** Same rule as LuckPerms' Fabric listener: only a source whose entity is a player has permissions. */
public final class MinecraftSubjectResolver implements SubjectResolver {
    @Override
    public Optional<Subject> resolve(Object source) {
        if (source instanceof CommandSourceStack stack && stack.getEntity() instanceof ServerPlayer player) {
            return Optional.of(new Subject(player.getUUID(), player));
        }
        return Optional.empty();
    }
}
