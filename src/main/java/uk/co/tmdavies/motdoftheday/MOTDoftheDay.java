package uk.co.tmdavies.motdoftheday;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import uk.co.tmdavies.motdoftheday.runnables.ChangeTask;
import uk.co.tmdavies.motdoftheday.utils.ConfigFile;
import uk.co.tmdavies.motdoftheday.utils.ConfigWatcher;

import java.util.Timer;
import java.util.TimerTask;

@Mod(MOTDoftheDay.MODID)
public class MOTDoftheDay {

    public static final String MODID = "motdoftheday";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ConfigFile CONFIG;
    public static TimerTask changeRunnable;
    public static boolean firstTime = true;
    public static Timer timer = new Timer();

    private ConfigWatcher watcher;


    public MOTDoftheDay(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Loading MOTDoftheDay...");

        CONFIG = new ConfigFile("config");
        watcher = new ConfigWatcher("config\\motdoftheday");

        watcher.watchFile();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Checks if mod is running from a dedicated server, if not disable.
        if (!event.getServer().isDedicatedServer()) {
            LOGGER.error("MOTDoftheDay can only be ran on a dedicated server.");

            NeoForge.EVENT_BUS.unregister(this);
            timer.cancel();

            return;
        }

        if (!CONFIG.isModEnabled()) {
            LOGGER.error("MOTDoftheDay is enabled in config.");

            NeoForge.EVENT_BUS.unregister(this);
            timer.cancel();

            return;
        }

        changeRunnable = new ChangeTask(event.getServer());

        CONFIG.loadConfig();
        timer.scheduleAtFixedRate(changeRunnable, 0, CONFIG.getChangeInterval());
    }

    public static void runChangeTask(int changeInterval) {
        timer.cancel();
        timer.scheduleAtFixedRate(changeRunnable, 0, changeInterval);
    }
}
