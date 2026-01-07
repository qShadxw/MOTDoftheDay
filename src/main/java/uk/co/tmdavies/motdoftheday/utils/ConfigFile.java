package uk.co.tmdavies.motdoftheday.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import uk.co.tmdavies.motdoftheday.MOTDoftheDay;

import java.io.*;
import java.nio.charset.Charset;
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
            setDefaults();
        }

        try (FileInputStream inputStream = new FileInputStream(this.path + "/" + this.fileName)) {
            this.jsonObj = JsonParser.parseString(IOUtils.toString(inputStream, Charset.defaultCharset())).getAsJsonObject();
        } catch (IOException e) {
            MOTDoftheDay.LOGGER.error("Error loading config file. Continuing to create new...");
        }

        MOTDoftheDay.runChangeTask(getChangeInterval());
        verboseConfig();
    }

    public void setDefaults() {
        this.jsonObj = new JsonObject();
        this.jsonObj.addProperty("Enabled", true);

        JsonObject motdSettings = new JsonObject();
        motdSettings.addProperty("Interval", 86400000);

        JsonArray messagesArray = new JsonArray();
        messagesArray.add("MOTD");
        messagesArray.add("Here");
        messagesArray.add("Bozo");
        messagesArray.add("xoxo");

        motdSettings.add("Messages", messagesArray);

        this.jsonObj.addProperty("_comment", "Default: 86400000 (in milliseconds)");
        this.jsonObj.add("MOTD", motdSettings);

        try (Writer writer = new FileWriter(this.path + "/" + this.fileName)) {
            writer.write(this.jsonObj.toString());
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

    public int getChangeInterval() {
        if (jsonObj == null) {
            MOTDoftheDay.LOGGER.error("Config was not loaded before grabbing data.");

            return Integer.MAX_VALUE;
        }

        JsonObject motdSettings = jsonObj.getAsJsonObject("MOTD");

        return motdSettings.get("Interval").getAsInt();
    }

    public List<String> getMessages() {
        if (jsonObj == null) {
            MOTDoftheDay.LOGGER.error("Config was not loaded before grabbing data.");

            return null;
        }

        JsonObject motdSettings = jsonObj.getAsJsonObject("MOTD");
        JsonArray messagesArray = motdSettings.get("Messages").getAsJsonArray();
        List<String> messages = new ArrayList<>();

        messagesArray.forEach(message -> messages.add(message.getAsString()));

        return messages;
    }

    public void verboseConfig() {
        if (jsonObj == null) {
            return;
        }

        boolean isModEnabled = isModEnabled();
        int changeInterval = getChangeInterval();
        List<String> messages = getMessages();

        MOTDoftheDay.LOGGER.info("Config Details:");
        MOTDoftheDay.LOGGER.info("IsModEnabled: " + isModEnabled);
        MOTDoftheDay.LOGGER.info("ChangeInterval: " + changeInterval);
        MOTDoftheDay.LOGGER.info("Messages: " + messages);
    }

}
