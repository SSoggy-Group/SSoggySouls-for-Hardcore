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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.configuration.file.FileConfiguration;

public class RPConfig {
    private static final HashMap<String, Set<Material>> defaultBlockTag = new HashMap<>(Map.ofEntries(
            Map.entry("soul-sand-blocktag", Set.of(Material.CRYING_OBSIDIAN, Material.OBSIDIAN)),
            Map.entry("flower-blocktag", Set.of(Material.SOUL_TORCH, Material.REDSTONE_TORCH)),
            Map.entry("ore-blocktag", Set.of(Material.ENCHANTING_TABLE)),
            Map.entry("fence-blocktag", Tag.FENCES.getValues()),
            Map.entry("stair-blocktag", Set.of(Material.MAGMA_BLOCK)))
    );
    private static final HashMap<String, Boolean> defaultConfigRules = new HashMap<>(Map.ofEntries(
            Map.entry("lose-inventory", false),
            Map.entry("restrict-menu-access", true),
            Map.entry("creative-players-drop-heads", false),
            Map.entry("keep-structure-base", true),
            Map.entry("head-effects", true),
            Map.entry("head-burns-in-lava", false), // Places playerhead when false
            Map.entry("ritual-lightning-strike", true), // Strike lightning on revival
            Map.entry("ritual-totem-effect", true), // Show totem animation on revival
            Map.entry("ghost-mode-particles", false)) // Show particles at death location for ghosts
    );
    private static final HashMap<String, Integer> defaultConfigTimers = new HashMap<>(Map.ofEntries(
            Map.entry("trusted-obituary-after", 60), // 1m
            Map.entry("friends-obituary-after", 600), // 10m
            Map.entry("public-obituary-after", 3600), // 60m
            Map.entry("spectator-headrestrict-radius", 16), // 16blocks
            Map.entry("revive-resistance-ticks", 100), // 5 seconds
            Map.entry("revive-glowing-ticks", 100)) // 5 seconds
    );

    static {
        RPStatic.BLOCK_TAGS = (Map<String, Set<Material>>) defaultBlockTag.clone(); // You forgot that java passes in references not objects
        RPStatic.CONFIG_RULES = (Map<String, Boolean>) defaultConfigRules.clone(); // This broke some stuff without the cloning
        RPStatic.CONFIG_TIMERS = (Map<String, Integer>) defaultConfigTimers.clone();
    }

    public static void init() {
        RPStatic.CLIENT.saveDefaultConfig();
        RPStatic.CLIENT.reloadConfig();
        FileConfiguration fileConfiguration = RPStatic.CLIENT.getConfig();
        RPStatic.BLOCK_TAGS.forEach((key, value) -> RPStatic.BLOCK_TAGS.put(key, fileConfiguration.getStringList(key).stream().map(Material::getMaterial).collect(Collectors.toSet())));
        RPStatic.CONFIG_RULES.forEach((key, value) -> RPStatic.CONFIG_RULES.put(key, fileConfiguration.getBoolean(key, value)));
        RPStatic.CONFIG_TIMERS.forEach((key, value) -> RPStatic.CONFIG_TIMERS.put(key, fileConfiguration.getInt(key, value)));
    }

    public static byte resetBlockTag(String where) {
        return defaultBlockTag.containsKey(where) ? setBlockTag(where, defaultBlockTag.get(where)) : 0;
    }

    public static byte setBlockTag(String where, Set<Material> who) {
        try {
            JavaPlugin instance = RPStatic.CLIENT;
            instance.getConfig().set(where, who.stream().map(Enum::name).toList());
            instance.saveConfig();
            instance.reloadConfig();
            RPStatic.BLOCK_TAGS.put(where, who);
            return 1;
        } catch (Exception e) {
            RPStatic.LOGGER.severe(e.getMessage());
            return 0;
        }
    }

    public static byte setConfigRule(String where, boolean who) {
        try {
            JavaPlugin instance = RPStatic.CLIENT;
            instance.getConfig().set(where, who);
            instance.saveConfig();
            instance.reloadConfig();
            RPStatic.CONFIG_RULES.put(where, who);
            return 1;
        } catch (Exception e) {
            RPStatic.LOGGER.severe(e.getMessage());
            return 0;
        }
    }

    public static byte setConfigTimer(String where, int who) {
        try {
            JavaPlugin instance = RPStatic.CLIENT;
            instance.getConfig().set(where, who);
            instance.saveConfig();
            instance.reloadConfig();
            RPStatic.CONFIG_TIMERS.put(where, who);
            return 1;
        } catch (Exception e) {
            RPStatic.LOGGER.severe(e.getMessage());
            return 0;
        }
    }
}
