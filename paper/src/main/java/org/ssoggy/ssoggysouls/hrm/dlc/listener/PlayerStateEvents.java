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

package org.ssoggy.ssoggysouls.hrm.dlc.listener;

import org.ssoggy.ssoggysouls.hrm.dlc.enums.GAMEMODESENUM;
import org.ssoggy.ssoggysouls.hrm.dlc.enums.STATSENUM;
import org.ssoggy.ssoggysouls.hrm.dlc.enums.COMMANDOUTPUTENUM;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPCommandOutput;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStatic;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStats;
import org.ssoggy.ssoggysouls.hrm.dlc.util.Pair;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.time.Instant;
import java.util.UUID;

public class PlayerStateEvents implements Listener {
    private static final String KEY_DEATHPOS = "deathpos";
    private static final String KEY_DEATHTIME = "deathtime";

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        if (!RPStatic.PLUGIN_HMODE) {
            return;
        }

        if (Boolean.TRUE.equals(RPStatic.CONFIG_RULES.getOrDefault("lose-inventory", false))) {
            event.getDrops().clear();
        }

        int minHeight = world.getMinHeight();
        int maxHeight = world.getMaxHeight();
        Location location = player.getLocation();
        Location deathPos = new Location(world, location.getBlockX(), Math.clamp(location.getY(), minHeight, maxHeight),
                location.getZ());
        String[] dimensionName = world.getKey().asString().split(":");
        if (dimensionName.length < 2)
            dimensionName = new String[] { "minecraft" + dimensionName[0] };
        RPCommandOutput deathMessage = new RPCommandOutput();
        deathMessage.success = COMMANDOUTPUTENUM.INFO;
        deathMessage.message = "<red>--- <bold>You have died!</bold> ---</red>\n<gray>Reclaim your loot at</gray> <gold><bold>X"
                + deathPos.getBlockX() + " Y" + deathPos.getBlockY() + " Z" + deathPos.getBlockZ()
                + "</bold></gold> <gray>inside</gray> " + dimensionName[0] + ":<gold><bold>" + dimensionName[1]
                + "</bold></gold>";
        player.sendRichMessage(deathMessage.toString());
        world.playSound(deathPos, Sound.ITEM_TRIDENT_THUNDER, SoundCategory.PLAYERS, 2, -2);
        world.playSound(deathPos, Sound.ENTITY_PIGLIN_DEATH, SoundCategory.PLAYERS, 2, -2);
        world.spawnParticle(Particle.DAMAGE_INDICATOR, deathPos, 100, 0.2, 0.4, 0.2);

        GAMEMODESENUM gamemode = GAMEMODESENUM.getPlayerGameMode(player);

        if (gamemode == GAMEMODESENUM.GHOSTMODE) {
            return;
        }

        if (gamemode == GAMEMODESENUM.CREATIVE
                && !Boolean.TRUE.equals(RPStatic.CONFIG_RULES.getOrDefault("creative-players-drop-heads", false))) {
            return;
        }

        Instant now = Instant.now();
        UUID uuid = player.getUniqueId();
        RPStatic.DEAD_LOCATIONS.put(uuid, Pair.of(deathPos, now));
        RPStatic.DEAD_STORAGE.setValue(uuid.toString(), KEY_DEATHPOS,
                deathPos.getBlockX() + "$" + deathPos.getBlockY() + "$" + deathPos.getBlockZ() + "$"
                        + deathPos.getWorld().getName());
        RPStatic.DEAD_STORAGE.setValue(uuid.toString(), KEY_DEATHTIME, now.toString());
        RPStatic.DEAD_STORAGE.saveConfig();

        RPStats stats = new RPStats(uuid);
        stats.incrementStat(STATSENUM.DEATHS, 1);

        Player killer = player.getKiller();
        if (killer != null) {
            RPStats killerStats = new RPStats(killer.getUniqueId());
            killerStats.incrementStat(STATSENUM.KILLS, 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        Location deathPos = RPStatic.DEAD_LOCATIONS
                .getOrDefault(player.getUniqueId(), Pair.of(world.getSpawnLocation(), Instant.now())).getLeft();
        event.setRespawnLocation(deathPos);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (GAMEMODESENUM.getPlayerGameMode(player) == GAMEMODESENUM.GHOSTMODE) {
            GAMEMODESENUM.setPlayerGameMode(player, GAMEMODESENUM.GHOSTMODE);
        }
    }

    @EventHandler
    private void onGameModeChanged(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        switch (event.getCause()) {
            case HARDCORE_DEATH:
                event.setCancelled(true);
                GAMEMODESENUM.setPlayerGameMode(player, GAMEMODESENUM.GHOSTMODE); // Uses the HRPXGAMEMODE class
                break;
            case COMMAND:
                event.setCancelled(true);
                GAMEMODESENUM.setPlayerGameMode(player, event.getNewGameMode()); // Uses the GameMode class
                break;
            default:
                break;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRevived(PlayerGameModeChangeEvent event) {
        if (event.getNewGameMode() != GameMode.SURVIVAL) {
            return;
        }

        Player player = event.getPlayer();
        // Run next tick so the SURVIVAL transition is fully applied before clearing DLC
        // ghost state.
        Bukkit.getScheduler().runTask(RPStatic.CLIENT, () -> {
            if (!player.isOnline() || player.getGameMode() != GameMode.SURVIVAL) {
                return;
            }
            if (GAMEMODESENUM.getPlayerGameMode(player) != GAMEMODESENUM.GHOSTMODE) {
                return;
            }

            UUID uuid = player.getUniqueId();
            GAMEMODESENUM.setPlayerGameMode(player, GAMEMODESENUM.SURVIVAL);
            RPStatic.DEAD_LOCATIONS.remove(uuid);
            RPStatic.DEAD_HOLDERS.remove(uuid);
            RPStatic.DEAD_STORAGE.removeValue(uuid.toString(), KEY_DEATHPOS);
            RPStatic.DEAD_STORAGE.removeValue(uuid.toString(), KEY_DEATHTIME);
            RPStatic.DEAD_STORAGE.saveConfig();
        });
    }
}
