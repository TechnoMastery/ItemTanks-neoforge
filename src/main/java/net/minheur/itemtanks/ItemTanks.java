package net.minheur.itemtanks;

import com.mojang.logging.LogUtils;
import net.minheur.itemtanks.file.Config;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ItemTanks.MODID)
public final class ItemTanks {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "itemtanks";

    // Directly reference a SLF4J logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public ItemTanks(@NonNull IEventBus modEventBus, @NonNull ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in.
        NeoForge.EVENT_BUS.register(this);
        // commands
        NeoForge.EVENT_BUS.addListener(TankCommands::register);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    public static void onServerStarted(@NonNull ServerStartedEvent event) {
        TankHandler.loadTanks(event.getServer());
    }
    @SubscribeEvent
    public static void onServerStopping(@NonNull ServerStoppingEvent event) {
        TankHandler.unloadTanks(event.getServer());
    }
}
