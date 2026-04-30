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

import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStatic;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class PlayerItemEvents implements Listener {
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemMeta meta = event.getItem().getItemStack().getItemMeta();
            if (meta instanceof SkullMeta skull) {
                OfflinePlayer skullOwner = skull.getOwningPlayer();
                if (skullOwner == null) return;

                UUID skulk_uuid = skullOwner.getUniqueId();
                UUID player_uuid = player.getUniqueId();

                RPStatic.DEAD_HOLDERS.put(skulk_uuid, player_uuid);
                RPStatic.DEAD_STORAGE.setValue(skulk_uuid.toString(), "deathholder", player_uuid.toString());
                RPStatic.DEAD_STORAGE.saveConfig();
            }
        }

    }
}
