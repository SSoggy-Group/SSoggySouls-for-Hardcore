/*
RevivePlus by Cera and Jakeccz
Copyright (C) 2026 Commune

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with RevivePlus.  If not, see <https://www.gnu.org/licenses/>
 */

package org.ssoggy.ssoggysouls.hrm.dlc.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RPStorage {

    private final File file;
    private final Logger logger;
    private FileConfiguration config;
    private final ExecutorService ioExecutor = new ThreadPoolExecutor(
            1,
            1,
            0L,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(1),
            new ThreadPoolExecutor.DiscardOldestPolicy()
    );


    public RPStorage(JavaPlugin plugin, String fileName) {
        this.logger = plugin.getLogger();
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        file = new File(plugin.getDataFolder(), fileName);

        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    logger.log(Level.WARNING, "Storage file already exists: {0}", fileName);
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, e, () -> "Could not create storage file: " + file.getPath());
            }
        }

        loadConfig();
    }

    public synchronized void loadConfig() {
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveConfig() {
        String data;
        synchronized (this) {
            data = config.saveToString();
        }
        ioExecutor.execute(() -> {
            try {
                Files.writeString(file.toPath(), data);
            } catch (IOException e) {
                logger.log(Level.SEVERE, e, () -> "Could not save configuration to " + file.getPath());
            }
        });
    }

    public synchronized void setValue(String table, String key, Object value) {
        String path = table + "." + key;
        config.set(path, value);
    }

    public synchronized void removeValue(String table, String key) {
        String path = table + "." + key;
        config.set(path, null);
    }

    @Nullable
    public synchronized String getValue(String table, String key) {
        return config.getString(table + "." + key, null);
    }

    public synchronized boolean hasValue(String table, String key, Object value) {
        String path = table + "." + key;
        return Objects.equals(config.getString(path, null), value);
    }

    public synchronized boolean hasValue(String table, String key) {
        return !hasValue(table, key, null);
    }

    public synchronized Map<String, Object> getTable(String table) throws NullPointerException {
        return config.getConfigurationSection(table).getValues(false);
    }

    public synchronized void shutdown() {
        ioExecutor.shutdown();
        try {
            if (!ioExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                logger.log(Level.WARNING, "RPStorage executor did not terminate in time for {0}; forcing shutdown", file.getName());
                ioExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "RPStorage shutdown interrupted for {0}; forcing shutdown", file.getName());
            ioExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}