package uk.co.tmdavies.motdoftheday.utils;

import com.google.gson.*;
import org.apache.commons.io.IOUtils;
import uk.co.tmdavies.motdoftheday.MOTDoftheDay;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Paths;

public class ConfigFile {

    private final String path;

    private JsonObject jsonObj;

    public ConfigFile(String path) {
        this.path = path;

        checkDir();
        loadConfig();
    }

    public void checkDir() {
        File dir = new File("config/motdoftheday");

        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    public void loadConfig() {
        try (FileInputStream inputStream = new FileInputStream(this.path)) {
            this.jsonObj = JsonParser.parseString(IOUtils.toString(inputStream, Charset.defaultCharset())).getAsJsonObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (this.jsonObj.isEmpty()) {
            setDefaults();
        }
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

        this.jsonObj.add("MOTD", motdSettings);

        try (Writer writer = new FileWriter(Paths.get("config/motdoftheday/config.json").toAbsolutePath().toString())) {
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

}
