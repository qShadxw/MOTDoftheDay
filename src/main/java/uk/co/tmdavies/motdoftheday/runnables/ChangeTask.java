package uk.co.tmdavies.motdoftheday.runnables;

import uk.co.tmdavies.motdoftheday.MOTDoftheDay;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ChangeTask implements Runnable {

    private final ThreadLocalRandom random;

    public ChangeTask() {
        this.random =  ThreadLocalRandom.current();
    }

    @Override
    public void run() {
        try {
            List<String> messages = MOTDoftheDay.configFile.getMessages();

            if (messages == null || messages.isEmpty()) {
                MOTDoftheDay.LOGGER.warn("No messages found in config for ChangeTask.");
                return;
            }

            int index = random.nextInt(messages.size());
            String newMOTD = messages.get(index);

            MOTDoftheDay.setMotd(newMOTD);
            MOTDoftheDay.LOGGER.info("Changing MOTD to {}.", newMOTD);
        } catch (Exception exception) {
            MOTDoftheDay.LOGGER.error("Error in ChangeTask");
            exception.printStackTrace();
        }
    }
}
