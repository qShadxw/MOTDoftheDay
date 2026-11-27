package uk.co.tmdavies.motdoftheday.runnables;

import com.google.gson.JsonObject;
import uk.co.tmdavies.motdoftheday.MOTDoftheDay;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ChangeTask implements Runnable {
    private static final ThreadLocalRandom random = ThreadLocalRandom.current();
    private static boolean overrideActive = false;

    @Override
    public void run() {
        updateMotd();
    }

    public static void updateMotd() {
        try {
            long now = Instant.now().getEpochSecond();
            String newMotd = null;
            boolean currentlyInOverride = false;

            for (JsonObject object : MOTDoftheDay.configFile.getOverrides()) {
                long timestamp = object.get("Timestamp").getAsLong();
                long durationSeconds = object.get("Duration").getAsLong();

                if (now >= timestamp && now <= timestamp + durationSeconds) {
                    newMotd = object.get("Message").getAsString();
                    currentlyInOverride = true;
                    break;
                }
            }

            if (!currentlyInOverride && (now >= MOTDoftheDay.nextIntervalTimestamp || MOTDoftheDay.getMotd() == null || overrideActive)) {
                List<String> messages = MOTDoftheDay.configFile.getMessages();

                if (messages != null && !messages.isEmpty()) {
                    String current = MOTDoftheDay.getMotd();
                    List<String> candidates = new ArrayList<>(messages);
                    candidates.removeIf(message -> message.equals(current));

                    if (candidates.isEmpty()) {
                        candidates = messages;
                    }

                    newMotd = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
                }
            }

            if (newMotd != null && !newMotd.equals(MOTDoftheDay.getMotd())) {
                MOTDoftheDay.setMotd(newMotd);
                MOTDoftheDay.updateServerMotd();
                MOTDoftheDay.LOGGER.info("Changing MOTD to {}.", newMotd);
            }

            overrideActive = currentlyInOverride;
        } catch (Exception exception) {
            MOTDoftheDay.LOGGER.error("Error updating MOTD", exception);
        }
    }

}
