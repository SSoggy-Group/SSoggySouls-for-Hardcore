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

import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.ssoggy.ssoggysouls.hrm.dlc.enums.GAMEMODESENUM;
import org.ssoggy.ssoggysouls.hrm.dlc.enums.COMMANDOUTPUTENUM;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPCommandOutput;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStatic;
import org.ssoggy.ssoggysouls.hrm.dlc.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

public class GhostModeEvents implements Listener {
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (GAMEMODESENUM.getPlayerGameMode(player) == GAMEMODESENUM.GHOSTMODE) {
            event.setCancelled(true);
        }
    }
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerAttack(PrePlayerAttackEntityEvent event) {
        Player player = event.getPlayer();
        if (GAMEMODESENUM.getPlayerGameMode(player) == GAMEMODESENUM.GHOSTMODE) {
            event.setCancelled(true);
        }
    }
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (GAMEMODESENUM.getPlayerGameMode(player) == GAMEMODESENUM.GHOSTMODE) {
            event.setCancelled(true);
        }
    }
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        Entity target = event.getRightClicked();
        if (GAMEMODESENUM.getPlayerGameMode(player) == GAMEMODESENUM.GHOSTMODE) {
            player.setGameMode(GameMode.SPECTATOR);
            player.setSpectatorTarget(target);
            RPCommandOutput message = new RPCommandOutput();
            message.success = COMMANDOUTPUTENUM.INFO;
            message.message = "Started spectating " + target.getName();
            player.sendRichMessage(message.toString());
            event.setCancelled(true);
        }
    }
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerExitSpectating(PlayerStopSpectatingEntityEvent event) {
        Player player = event.getPlayer();
        if (GAMEMODESENUM.getPlayerGameMode(player) == GAMEMODESENUM.GHOSTMODE && player.getGameMode() == GameMode.SPECTATOR) {
            player.setGameMode(GameMode.ADVENTURE);
        }
    }
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();


        if (GAMEMODESENUM.getPlayerGameMode(player) != GAMEMODESENUM.GHOSTMODE) {
            return;
        }

        if (!event.hasChangedBlock()) { // Ignore rotational differences
            return;
        }

        UUID uuid = player.getUniqueId();
        Location player_location = player.getLocation();
        Pair<Location, Instant> pair = RPStatic.DEAD_LOCATIONS.get(uuid);

        Location dead_location = getLocation(pair, uuid, player); // See method below
        if (dead_location == null) return;

        double distance = dead_location.distanceSquared(player_location);
        double max_distance = (double) RPStatic.CONFIG_TIMERS.getOrDefault("spectator-headrestrict-radius", 16);
        if (distance >= (max_distance * max_distance)) {
            dead_location.setYaw(player.getYaw());
            dead_location.setPitch(player.getPitch());
            player.teleportAsync(dead_location); // TODO: This should slowly increase overtime

            //player.getWorld().spawnParticle(Particle.DRAGON_BREATH, dead_location, 50, 0, 1, 0, 0.2);
            player.playSound(player, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1, 0);
            RPCommandOutput message = new RPCommandOutput();
            message.success = COMMANDOUTPUTENUM.FALSE;
            message.message = "<gray>You may not travel that far away from your death location</gray>";
            player.sendRichMessage(message.toString());
            event.setCancelled(true);
        }
    }

    private @Nullable Location getLocation(Pair<Location, Instant> pair, UUID uuid, Player player) {
        if (pair != null) return pair.getLeft();
        if (RPStatic.DEAD_HOLDERS.containsKey(uuid)) {
            UUID holder = RPStatic.DEAD_HOLDERS.get(uuid);
            if (holder == null) return null;

            return player.getServer().getOfflinePlayer(holder).getLocation();
        }

        String holder = RPStatic.DEAD_STORAGE.getValue(uuid.toString(), "deathholder");

        if (holder != null) {
            Location offline_location = Bukkit.getOfflinePlayer(UUID.fromString(holder)).getLocation();
            if (offline_location != null)
                return offline_location;
        }

        String saved_time = RPStatic.DEAD_STORAGE.getValue(uuid.toString(), "deathtime");
        String saved_pos = RPStatic.DEAD_STORAGE.getValue(uuid.toString(), "deathpos");
        if (saved_pos == null || saved_time == null) {
            return null;
        }

        String[] split = saved_pos.split("\\$");
        if (split.length < 3) {
            return null;
        }

        try {
            Location new_location = new Location(player.getWorld(),
                    (int) Double.parseDouble(split[0]) + 0.5, (int) Double.parseDouble(split[1]) + 0.5, (int) Double.parseDouble(split[2]) + 0.5); // center transforms (e.g. 4.79530 or 4.12405 to 4 to 4.5)
            RPStatic.DEAD_LOCATIONS.put(uuid, Pair.of(new_location, Instant.parse(saved_time))); // This should be removed when playerhead block is destroyed
            return new_location;
        } catch (Exception ignored) {
            return null;
        }
    }
}
