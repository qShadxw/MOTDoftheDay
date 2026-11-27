package uk.co.tmdavies.motdoftheday.files;

import com.google.gson.*;
import org.apache.commons.io.IOUtils;
import uk.co.tmdavies.motdoftheday.MOTDoftheDay;

import java.io.*;
import java.nio.charset.Charset;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConfigFile {

    private final String path;
    private final String fileName;
    private File file;

    private JsonObject jsonObj;

    public ConfigFile(String name) {
        if (!name.endsWith(".json")) {
            name = name + ".json";
        }

        this.path = "./config/motdoftheday";
        this.fileName = name;
        this.file = new File(this.path + "/" + this.fileName);

        checkDir();
        checkFile();
    }

    public void checkDir() {
        File dir = new File(this.path);

        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    public void checkFile() {
        if (file.exists()) {
            return;
        }

        try {
            file.createNewFile();
        } catch (IOException exception) {
            MOTDoftheDay.LOGGER.error("Error creating config file.");
            exception.printStackTrace();
        }
    }

    public void loadConfig() {
        this.file = new File(this.path + "/" + this.fileName);

        if (file.length() == 0) {
            setConfigDefaults();
        }

        try (FileInputStream inputStream = new FileInputStream(this.path + "/" + this.fileName)) {
            this.jsonObj = JsonParser.parseString(IOUtils.toString(inputStream, Charset.defaultCharset())).getAsJsonObject();
        } catch (IOException e) {
            MOTDoftheDay.LOGGER.error("Error loading config file. Continuing to create new...");
        }

        long now = Instant.now().getEpochSecond();
        MOTDoftheDay.nextIntervalTimestamp = getNextChangeTimestamp(now);

        verboseConfig();
        MOTDoftheDay.runChangeTask();
    }

    public void setConfigDefaults() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        this.jsonObj = new JsonObject();
        this.jsonObj.addProperty("Enabled", true);

        JsonObject motdSettings = new JsonObject();
        motdSettings.addProperty("Interval", 86400);
        motdSettings.addProperty("AlignToMidnight", true);
        motdSettings.addProperty("TimeZone", "");

        JsonArray messagesArray = new JsonArray();
        messagesArray.add("MOTD");
        messagesArray.add("Here");
        messagesArray.add("Bozo");
        messagesArray.add("xoxo");

        motdSettings.add("Messages", messagesArray);

        JsonObject overrideSettings = new JsonObject();
        overrideSettings.addProperty("_Comment", "These do not remember on start-up. You re-set them after restarting your server.");

        JsonObject firstOverride = new JsonObject();
        firstOverride.addProperty("Message", "This is a placeholder override, please replace.");
        firstOverride.addProperty("Timestamp", Instant.now().getEpochSecond() + 30);
        firstOverride.addProperty("_Comment", "Duration is in seconds.");
        firstOverride.addProperty("Duration", 60);

        JsonObject secondOverride = new JsonObject();
        secondOverride.addProperty("Message", "Happy Easter.");
        secondOverride.addProperty("Timestamp", 1775343600);
        secondOverride.addProperty("_Comment", "Duration is in seconds.");
        secondOverride.addProperty("Duration", 86400);

        overrideSettings.add("Placeholder", firstOverride);
        overrideSettings.add("Easter", secondOverride);

        this.jsonObj.addProperty("_Comment", "Default: 86400 (in seconds)");
        this.jsonObj.add("MOTD", motdSettings);
        this.jsonObj.add("Overrides", overrideSettings);

        try (Writer writer = new FileWriter(this.path + "/" + this.fileName)) {
            gson.toJson(this.jsonObj, writer);
        } catch (IOException exception) {
            MOTDoftheDay.LOGGER.error("Failed to write json file defaults.");
            exception.printStackTrace();
        }
    }

    public JsonObject getConfig() {
        if (jsonObj == null) {
            MOTDoftheDay.LOGGER.error("Config was not loaded before getting.");

            return null;
        }

        return jsonObj;
    }

    public Object get(String path) {
        if (jsonObj == null) {
            MOTDoftheDay.LOGGER.error("Config was not loaded before grabbing data.");

            return null;
        }

        return jsonObj.get(path);
    }

    public boolean isModEnabled() {
        if (jsonObj == null) {
            MOTDoftheDay.LOGGER.error("Config was not loaded before grabbing data.");

            return true;
        }

        return jsonObj.get("Enabled").getAsBoolean();
    }

    public long getChangeInterval() {
        if (jsonObj == null) {
            MOTDoftheDay.LOGGER.error("Config was not loaded before grabbing data.");

            return Long.MAX_VALUE;
        }

        JsonObject motdSettings = jsonObj.getAsJsonObject("MOTD");

        return motdSettings.get("Interval").getAsLong();
    }

    public boolean isAlignedToMidnight() {
        if (jsonObj == null) {
            return true;
        }

        JsonElement element = jsonObj.getAsJsonObject("MOTD").get("AlignToMidnight");

        return element == null || element.getAsBoolean();
    }

    public ZoneId getZoneId() {
        if (jsonObj != null) {
            JsonElement element = jsonObj.getAsJsonObject("MOTD").get("TimeZone");
            String zoneName = element == null ? "" : element.getAsString().trim();

            if (!zoneName.isEmpty()) {
                try {
                    return ZoneId.of(zoneName);
                } catch (DateTimeException exception) {
                    MOTDoftheDay.LOGGER.warn("Invalid TimeZone '{}' in config, using the server's timezone.", zoneName);
                }
            }
        }

        return ZoneId.systemDefault();
    }

    public long getNextChangeTimestamp(long nowEpochSeconds) {
        long interval = getChangeInterval();

        if (jsonObj == null || !isAlignedToMidnight() || interval < 86400L) {
            return nowEpochSeconds + interval;
        }

        return nextMidnight(nowEpochSeconds, interval, getZoneId());
    }

    /** Pure helper (no config access) so the maths can be tested on its own. */
    static long nextMidnight(long nowEpochSeconds, long intervalSeconds, ZoneId zone) {
        long days = Math.max(1L, intervalSeconds / 86400L);
        ZonedDateTime now = Instant.ofEpochSecond(nowEpochSeconds).atZone(zone);

        return now.toLocalDate().plusDays(days).atStartOfDay(zone).toEpochSecond();
    }

    public List<String> getMessages() {
        if (jsonObj == null) {
            MOTDoftheDay.LOGGER.error("Config was not loaded before grabbing data.");

            return null;
        }

        JsonObject motdSettings = jsonObj.getAsJsonObject("MOTD");
        JsonArray messagesArray = motdSettings.get("Messages").getAsJsonArray();
        List<String> messages = new ArrayList<>();

        messagesArray.forEach(message -> {
            if (message.isJsonNull()) {
                return;
            }

            messages.add(message.getAsString());
        });

        return messages;
    }

    public List<JsonObject> getOverrides() {
        JsonObject overrides = jsonObj.getAsJsonObject("Overrides");
        List<JsonObject> overrideList = new ArrayList<>();

        overrides.keySet().forEach(keys -> {
            if (keys.equals("_Comment")) {
                return;
            }

            overrideList.add(overrides.getAsJsonObject(keys));
        });

        return overrideList;
    }

    public void verboseConfig() {
        if (jsonObj == null) {
            return;
        }

        MOTDoftheDay.LOGGER.info("Config Details:");
        MOTDoftheDay.LOGGER.info("IsModEnabled: " + isModEnabled());
        MOTDoftheDay.LOGGER.info("ChangeInterval: " + getChangeInterval());
        MOTDoftheDay.LOGGER.info("AlignToMidnight: " + isAlignedToMidnight());
        MOTDoftheDay.LOGGER.info("TimeZone: " + getZoneId());
        MOTDoftheDay.LOGGER.info("Next change at: " + Instant.ofEpochSecond(getNextChangeTimestamp(Instant.now().getEpochSecond())).atZone(getZoneId()));
        MOTDoftheDay.LOGGER.info("Messages: " + getMessages());
        MOTDoftheDay.LOGGER.info("Overrides >");

        for (JsonObject object : getOverrides()) {
            MOTDoftheDay.LOGGER.info("> Message: {}", object.get("Message").getAsString());
            MOTDoftheDay.LOGGER.info("> Timestamp: {}", object.get("Timestamp").getAsString());
            MOTDoftheDay.LOGGER.info("> Duration: {}", object.get("Duration").getAsString());
            MOTDoftheDay.LOGGER.info("");
        }
    }

}