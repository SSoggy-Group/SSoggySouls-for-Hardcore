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

import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.Location;
import org.bukkit.Material;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

@SuppressWarnings({"java:S1104", "java:S1444", "java:S3008", "java:S2386"}) // Intentional: runtime-initialized singletons, not true constants
public class RPStatic {
    private RPStatic() {}
    public static String PREFIX = "<black>[</black><dark_purple>RevivalPlus</dark_purple><black>]</black>";
    public static String PLUGIN_ID;
    public static JavaPlugin CLIENT;
    public static Logger LOGGER;
    public static boolean PLUGIN_HMODE;

    public static RPStorage DEAD_STORAGE;
    public static Map<UUID, Pair<Location, Instant>> DEAD_LOCATIONS;
    public static Map<UUID, UUID> DEAD_HOLDERS;

    public static Map<String, Set<Material>> BLOCK_TAGS;
    public static Map<String, Boolean> CONFIG_RULES;
    public static Map<String, Integer> CONFIG_TIMERS;

    public static RPStorage SOCIAL_STORAGE;
    public static RPStorage USERNAME_CACHE;
    public static RPStorage STATS_STORAGE;

    public static void init(JavaPlugin plugin) {
        RPStatic.PLUGIN_ID = "revivalplus"; // DO NOT CHANGE
        RPStatic.CLIENT = plugin;
        RPStatic.LOGGER = plugin.getLogger();
        RPStatic.PLUGIN_HMODE = plugin.getServer().isHardcore();

        RPStatic.DEAD_STORAGE = new RPStorage(plugin, "deaths.yml");
        RPStatic.DEAD_HOLDERS = new HashMap<>();
        RPStatic.DEAD_LOCATIONS = new HashMap<>();

        RPStatic.BLOCK_TAGS = new HashMap<>();
        RPStatic.CONFIG_RULES = new HashMap<>();
        RPStatic.CONFIG_TIMERS = new HashMap<>();

        RPStatic.SOCIAL_STORAGE = new RPStorage(plugin, "social.yml");

        RPStatic.USERNAME_CACHE = new RPStorage(plugin, "usernamecache.yml");
        RPStatic.STATS_STORAGE = new RPStorage(plugin, "stats.yml");
    }
}
