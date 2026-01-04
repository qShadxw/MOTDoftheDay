package uk.co.tmdavies.motdoftheday.utils;

import uk.co.tmdavies.motdoftheday.MOTDoftheDay;

import java.io.IOException;
import java.nio.file.*;

public class ConfigWatcher {

    private final Path path;

    public ConfigWatcher(String path) {
        this.path = Paths.get(path);
    }

    public void watchFile() {
        try {
            FileSystem fileSystem = FileSystems.getDefault();
            WatchService watchService = fileSystem.newWatchService();

            path.register(watchService,
                    StandardWatchEventKinds.ENTRY_MODIFY
            );

            new Thread(() -> {
                try {
                    while (true) {
                        WatchKey key = watchService.take();

                        for (WatchEvent<?> event : key.pollEvents()) {
                            Object context = event.context();
                            if (context.toString().equals("config.json")) {
                                configFileChanged();
                            }
                        }

                        key.reset();
                    }
                } catch (InterruptedException exception) {
                    MOTDoftheDay.LOGGER.error("Failed to loop watch service.");
                    exception.printStackTrace();
                }
            }).start();
        } catch (IOException exception) {
            MOTDoftheDay.LOGGER.error("Failed to create watch service.");
            exception.printStackTrace();
        }
    }

    public void configFileChanged() {
        MOTDoftheDay.LOGGER.info("Config File Modified");
        MOTDoftheDay.CONFIG.loadConfig();
    }

}
