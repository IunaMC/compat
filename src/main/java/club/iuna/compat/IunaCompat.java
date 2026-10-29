package club.iuna.compat;

import club.iuna.compat.command.CompatCommand;
import club.iuna.compat.module.CompatModule;
import club.iuna.compat.permissions.PermissionsModule;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;

import java.util.List;

/**
 * Server-side compatibility layer for the IUNA modpack. Each fix is a {@link CompatModule}; a new
 * one is added to {@link #MODULES} and nothing else has to change.
 */
@Mod(value = IunaCompat.MOD_ID, dist = Dist.DEDICATED_SERVER)
public final class IunaCompat {
    public static final String MOD_ID = "iunacompat";
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final PermissionsModule PERMISSIONS = new PermissionsModule();
    private static final List<CompatModule> MODULES = List.of(PERMISSIONS);

    public IunaCompat(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, CompatConfig.SPEC);
        modBus.addListener(this::onCommonSetup);
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    public static List<CompatModule> modules() {
        return MODULES;
    }

    public static PermissionsModule permissions() {
        return PERMISSIONS;
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        // Every mod is constructed by now, so the Fabric mods' classes are visible. A module that
        // fails reports it in its status and never takes the server down with it.
        event.enqueueWork(() -> MODULES.forEach(CompatModule::enable));
    }

    private void onServerStarted(ServerStartedEvent event) {
        PERMISSIONS.registerKnownNodes();
        LOGGER.info("IUNA Compat ready: {} module(s)", MODULES.size());
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        CompatCommand.register(event.getDispatcher());
    }
}
