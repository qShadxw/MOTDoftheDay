package uk.co.tmdavies.motdoftheday.runnables;

import net.minecraft.server.MinecraftServer;
import uk.co.tmdavies.motdoftheday.MOTDoftheDay;

import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class ChangeTask extends TimerTask {

    private final MinecraftServer server;

    public ChangeTask(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        MOTDoftheDay.isTimerRunning = true;

        if (this.server.isCurrentlySaving() || this.server.isShutdown() || this.server.isStopped()) {
            MOTDoftheDay.LOGGER.info("Server is closing/closed. Cancelling MOTD change.");
            this.cancel();

            return;
        }

        List<String> motdList = MOTDoftheDay.CONFIG.getMessages();
        String newMotd = motdList.get(ThreadLocalRandom.current().nextInt(motdList.size()-1));
        newMotd = newMotd.replace('&', '§');

        MOTDoftheDay.LOGGER.info("Changing MOTD to {}.", newMotd);

        try {
            this.server.setMotd(newMotd);
        } catch (RuntimeException exception) {
            MOTDoftheDay.LOGGER.error("Failed to change MOTD.");
            exception.printStackTrace();
        }
    }
}
