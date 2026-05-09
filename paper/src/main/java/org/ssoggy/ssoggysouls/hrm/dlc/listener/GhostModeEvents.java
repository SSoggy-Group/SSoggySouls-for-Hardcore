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
    private void cancelEventIfGhostMode(Player player, org.bukkit.event.Cancellable event) {
        if (GAMEMODESENUM.getPlayerGameMode(player) == GAMEMODESENUM.GHOSTMODE) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        cancelEventIfGhostMode(player, event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerAttack(PrePlayerAttackEntityEvent event) {
        cancelEventIfGhostMode(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        cancelEventIfGhostMode(player, event);
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
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerStopSpectatingEntity(PlayerStopSpectatingEntityEvent event) {
        Player player = event.getPlayer();
        if (GAMEMODESENUM.getPlayerGameMode(player) == GAMEMODESENUM.GHOSTMODE) {
            Entity target = event.getSpectatorTarget();
            if (target instanceof Player) {
                Player targetPlayer = (Player) target;
                if (!targetPlayer.isOnline()) {
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (GAMEMODESENUM.getPlayerGameMode(player) == GAMEMODESENUM.GHOSTMODE && player.getGameMode() != GameMode.SPECTATOR) {
            player.setGameMode(GameMode.SPECTATOR);
        }
    }
}
