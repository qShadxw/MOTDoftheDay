package uk.co.tmdavies.motdoftheday;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import uk.co.tmdavies.motdoftheday.runnables.ChangeRunnable;

@Mod(MOTDoftheDay.MODID)
public class MOTDoftheDay {

    public static final String MODID = "motdoftheday";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static Runnable changeRunnable;
    public static Thread runnableThread;

    public MOTDoftheDay(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, MOTDConfig.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        double startingTime = System.currentTimeMillis();

        LOGGER.info("Loading MOTD List from Config...");
        //MOTDConfig.loadMOTDList();

        double loadingTime = System.currentTimeMillis() - startingTime;
        LOGGER.error("Successfully loaded MOTD List from config in {}ms.", loadingTime);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");

        // Checks if mod is running from a dedicated server, if not disable.
        if (!event.getServer().isDedicatedServer()) {
            LOGGER.error("MOTDoftheDay can only be ran on a dedicated server.");
            NeoForge.EVENT_BUS.unregister(this);

            return;
        }

        if (!MOTDConfig.ENABLED.get()) {
            LOGGER.error("MOTDoftheDay is enabled in config.");
            NeoForge.EVENT_BUS.unregister(this);

            return;
        }

        // Starting Runnable
        changeRunnable = new ChangeRunnable(event.getServer());
        runnableThread = new Thread(changeRunnable);

        runnableThread.start();
    }
}
