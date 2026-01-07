package uk.co.tmdavies.motdoftheday;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import uk.co.tmdavies.motdoftheday.runnables.ChangeTask;
import uk.co.tmdavies.motdoftheday.utils.ConfigFile;
import uk.co.tmdavies.motdoftheday.utils.ConfigWatcher;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Mod(MOTDoftheDay.MODID)
public class MOTDoftheDay {

    public static final String MODID = "motdoftheday";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final ScheduledExecutorService SCHEDULER =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread changeTaskThread = new Thread(r, "MOTD-ChangeTask");
                changeTaskThread.setDaemon(true);

                return changeTaskThread;
            });

    public static ConfigFile configFile;
    public static MinecraftServer minecraftServer;

    private static ScheduledFuture<?> changeTaskFuture;

    public MOTDoftheDay(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Loading MOTDoftheDay...");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        configFile = new ConfigFile("config");
        ConfigWatcher watcher = new ConfigWatcher("./config/motdoftheday");
        minecraftServer = event.getServer();

        configFile.loadConfig();

        if (!event.getServer().isDedicatedServer()) {
            LOGGER.error("MOTDoftheDay can only be ran on a dedicated server.");

            NeoForge.EVENT_BUS.unregister(this);
            changeTaskFuture.cancel(true);
            SCHEDULER.close();

            return;
        }

        if (!configFile.isModEnabled()) {
            LOGGER.error("MOTDoftheDay is enabled in config.");

            NeoForge.EVENT_BUS.unregister(this);
            changeTaskFuture.cancel(true);
            SCHEDULER.close();

            return;
        }

        watcher.watchFile();
    }

    public static void setMotd(String newMotd) {
        minecraftServer.setMotd(newMotd);
    }

    public static void runChangeTask(int changeInterval) {
        if (changeTaskFuture != null && !changeTaskFuture.isCancelled()) {
            changeTaskFuture.cancel(false);
        }

        changeTaskFuture = SCHEDULER.scheduleAtFixedRate(
                new ChangeTask(),
                0,
                changeInterval,
                TimeUnit.MILLISECONDS
        );
    }
}
