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
import java.util.Map;

public class RPStorage {

    private final File file;
    public FileConfiguration config;


    public RPStorage(JavaPlugin plugin, String File) {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        file = new File(plugin.getDataFolder(), File);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        loadConfig();
    }

    public void loadConfig() {
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setValue(String table, String key, Object value) {
        String path = table + "." + key;
        config.set(path, value);
    }

    public void removeValue(String table, String key) {
        String path = table + "." + key;
        config.set(path, null);
    }

    @Nullable
    public String getValue(String table, String key) {
        return config.getString(table + "." + key, null);
    }

    public boolean hasValue(String table, String key, Object value) {
        String path = table + "." + key;
        return config.getString(path, null) == value;
    }

    public boolean hasValue(String table, String key) {
        return !hasValue(table, key, null);
    }

    public Map<String, Object> getTable(String table) throws NullPointerException {
        return config.getConfigurationSection(table).getValues(false);
    }
}