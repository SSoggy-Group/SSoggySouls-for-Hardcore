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

package org.ssoggy.ssoggysouls.hrm.dlc;

import org.ssoggy.ssoggysouls.hrm.dlc.commands.GhostModeCommand;
import org.ssoggy.ssoggysouls.hrm.dlc.commands.RPConfigCommand;
import org.ssoggy.ssoggysouls.hrm.dlc.commands.ObituariesCommand;
import org.ssoggy.ssoggysouls.hrm.dlc.commands.SocialCommand;
import org.ssoggy.ssoggysouls.hrm.dlc.listener.*;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPConfig;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStatic;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStorage;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class RevivalPlus {
    private RevivalPlus() {}

    public static void enable(JavaPlugin plugin) {
        RPStatic.init(plugin);
        RPConfig.init();

        if (!RPStatic.PLUGIN_HMODE) {
            RPStatic.LOGGER.warning("Hardcore Mode is currently disabled, certain plugin functionalities will fail to run.");
        }


        PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvents(new BlockEvents(), plugin);
        pluginManager.registerEvents(new PlayerItemEvents(), plugin);
        pluginManager.registerEvents(new PlayerStateEvents(), plugin);
        pluginManager.registerEvents(new GhostModeEvents(), plugin);

        java.util.Objects.requireNonNull(plugin.getCommand("revivalconfig")).setExecutor(new RPConfigCommand());
        java.util.Objects.requireNonNull(plugin.getCommand("deathlist")).setExecutor(new ObituariesCommand());
        java.util.Objects.requireNonNull(plugin.getCommand("ghostmode")).setExecutor(new GhostModeCommand());
        java.util.Objects.requireNonNull(plugin.getCommand("trust")).setExecutor(new SocialCommand());
    }

    public static void disable() {
        shutdownStorage(RPStatic.DEAD_STORAGE);
        shutdownStorage(RPStatic.STATS_STORAGE);
        shutdownStorage(RPStatic.SOCIAL_STORAGE);
        shutdownStorage(RPStatic.USERNAME_CACHE);
    }

    private static void shutdownStorage(RPStorage storage) {
        if (storage == null) return;
        try {
            storage.shutdown();
        } catch (Exception e) {
            RPStatic.LOGGER.log(java.util.logging.Level.SEVERE, "Error shutting down RPStorage", e);
        }
    }
}
