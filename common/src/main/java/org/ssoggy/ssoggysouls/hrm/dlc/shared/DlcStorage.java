package org.ssoggy.ssoggysouls.hrm.dlc.shared;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DlcStorage {
    private final File file;
    private final Logger logger;
    private final Properties properties = new Properties();

    public DlcStorage(File folder, String fileName, Logger logger) {
        this.file = new File(folder, fileName);
        this.logger = logger;
        if (!folder.exists() && !folder.mkdirs()) {
            logger.warning("Could not create RevivalPlus data folder: " + folder.getPath());
        }
        load();
    }

    public synchronized void load() {
        properties.clear();
        if (!file.exists()) {
            save();
            return;
        }

        try (FileInputStream input = new FileInputStream(file)) {
            properties.load(input);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e, () -> "Could not load RevivalPlus storage " + file.getPath());
        }
    }

    public synchronized void save() {
        try (FileOutputStream output = new FileOutputStream(file)) {
            properties.store(output, "SSoggySouls RevivalPlus data");
        } catch (IOException e) {
            logger.log(Level.SEVERE, e, () -> "Could not save RevivalPlus storage " + file.getPath());
        }
    }

    public synchronized void setValue(String table, String key, Object value) {
        String path = path(table, key);
        if (value == null) {
            properties.remove(path);
            return;
        }
        properties.setProperty(path, String.valueOf(value));
    }

    public synchronized boolean setValueIfChanged(String table, String key, Object value) {
        String path = path(table, key);
        if (value == null) {
            return properties.remove(path) != null;
        }
        String strValue = String.valueOf(value);
        if (Objects.equals(properties.getProperty(path), strValue)) {
            return false;
        }
        properties.setProperty(path, strValue);
        return true;
    }

    public synchronized void removeValue(String table, String key) {
        properties.remove(path(table, key));
    }

    public synchronized String getValue(String table, String key) {
        return properties.getProperty(path(table, key));
    }

    public synchronized boolean hasValue(String table, String key) {
        return properties.containsKey(path(table, key));
    }

    public synchronized boolean hasValue(String table, String key, Object value) {
        return Objects.equals(properties.getProperty(path(table, key)), value == null ? null : String.valueOf(value));
    }

    public synchronized Map<String, String> getTable(String table) {
        String prefix = table + ".";
        Map<String, String> values = new HashMap<>();
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith(prefix)) {
                values.put(key.substring(prefix.length()), properties.getProperty(key));
            }
        }
        return values;
    }

    public synchronized Set<String> getTables() {
        Set<String> tables = new HashSet<>();
        for (String key : properties.stringPropertyNames()) {
            int dot = key.indexOf('.');
            if (dot > 0) {
                tables.add(key.substring(0, dot));
            }
        }
        return tables;
    }

    private static String path(String table, String key) {
        return table + "." + key;
    }
}
