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

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiPredicate;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class RPUtil {
    private static final String KEY_USERNAME_CACHE = "usernamecache";
    private RPUtil() {}

    public static String addUsernameToCache(UUID uuid) {
        String uuidString = uuid.toString();
        try {
            String username = Bukkit.getOfflinePlayer(uuid).getName();
            if (RPStatic.USERNAME_CACHE.setValueIfChanged(KEY_USERNAME_CACHE, uuidString, username)) {
                RPStatic.USERNAME_CACHE.saveConfig();
            }
            return username;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getUsernameFromCache(UUID uuid) {
        String uuidString = uuid.toString();
        if (!RPStatic.USERNAME_CACHE.hasValue(KEY_USERNAME_CACHE, uuidString)) {
            return addUsernameToCache(uuid);
        }
        return RPStatic.USERNAME_CACHE.getValue(KEY_USERNAME_CACHE, uuidString);
    }
    public static Map<UUID, String> getAllUsernamesFromCache(@Nullable BiPredicate<? super UUID, ? super String> filter) {
        try {
            Map<UUID, String> result = Maps.newHashMap();
            RPStatic.USERNAME_CACHE.getTable(KEY_USERNAME_CACHE).forEach((rawKey, rawValue) -> {
                try {
                    UUID k = UUID.fromString(rawKey);
                    String v = (String) rawValue;
                    if (filter == null || filter.test(k, v)) {
                        result.put(k, v);
                    }
                } catch (Exception e) { // Skip if invalid
                }
            });
            return result;
        } catch (NullPointerException ignored) {
            return Maps.newHashMapWithExpectedSize(0);
        }
    }

    public static ItemStack createSkullWithName(@NotNull String name) {
        ItemStack skullHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta)skullHead.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(name));
        meta.lore(List.of(Component.text("Used to revive this player")));
        skullHead.setItemMeta(meta);
        return skullHead;
    }
    public static Block createSkullBlockWithName(@NotNull String name, @NotNull Location location) {
        Location loc = location.clone();
        Block sBlock = loc.getBlock();
        sBlock.setType(Material.PLAYER_HEAD);
        Skull sBlockState = (Skull) sBlock.getState();

        sBlockState.setOwningPlayer(Bukkit.getOfflinePlayer(name));
        sBlockState.update(true);
        return sBlock;
    }
}
