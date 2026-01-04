package uk.co.tmdavies.motdoftheday.runnables;

import net.minecraft.server.MinecraftServer;
import uk.co.tmdavies.motdoftheday.MOTDConfig;
import uk.co.tmdavies.motdoftheday.MOTDoftheDay;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ChangeRunnable implements Runnable {

    private final MinecraftServer server;

    public ChangeRunnable(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        MOTDoftheDay.LOGGER.info("Changing MOTD...");

        if (this.server == null) {
            MOTDoftheDay.LOGGER.error("Failed to grab server. Cancelling MOTD change.");
            return;
        }

        List<String> motdList = (List<String>) MOTDConfig.MOTD_STRINGS.get();
        String newMotd = motdList.get(ThreadLocalRandom.current().nextInt(motdList.size())-1);

        MOTDoftheDay.LOGGER.info("Changing MOTD to {}.", newMotd);

        try {
            this.server.setMotd(newMotd);
        } catch (RuntimeException exception) {
            MOTDoftheDay.LOGGER.error("Failed to change MOTD.");
            exception.printStackTrace();
        }

        try {
            Thread.sleep(MOTDConfig.CHANGE_TIME.get());
        } catch (InterruptedException exception) {
            MOTDoftheDay.LOGGER.error("Failed to sleep thread.");
            exception.printStackTrace();
        }
    }
}
