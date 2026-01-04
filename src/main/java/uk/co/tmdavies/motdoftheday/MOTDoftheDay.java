package uk.co.tmdavies.motdoftheday;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import uk.co.tmdavies.motdoftheday.runnables.ChangeTask;

import java.util.Timer;
import java.util.TimerTask;

@Mod(MOTDoftheDay.MODID)
public class MOTDoftheDay {

    public static final String MODID = "motdoftheday";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static TimerTask changeRunnable;
    public static Timer timer = new Timer();

    public MOTDoftheDay(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, MOTDConfig.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Loading MOTDoftheDay...");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Checks if mod is running from a dedicated server, if not disable.
        if (!event.getServer().isDedicatedServer()) {
            LOGGER.error("MOTDoftheDay can only be ran on a dedicated server.");
            NeoForge.EVENT_BUS.unregister(this);

            return;
        }

        if (!MOTDConfig.ENABLED.getAsBoolean()) {
            LOGGER.error("MOTDoftheDay is enabled in config.");
            NeoForge.EVENT_BUS.unregister(this);

            return;
        }
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        // Starting Runnable
        double startingTime = System.currentTimeMillis();
        changeRunnable = new ChangeTask(event.getServer());

        timer.scheduleAtFixedRate(changeRunnable, 0, MOTDConfig.CHANGE_TIME.getAsInt());

        double loadingTime = System.currentTimeMillis() - startingTime;
        LOGGER.error("Finished Loading MOTDoftheDay Schedules {}ms.", loadingTime);
    }
}
