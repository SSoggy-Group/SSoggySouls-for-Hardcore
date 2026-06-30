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

package org.ssoggy.ssoggysouls.hrm.dlc.enums;

import com.google.common.collect.Maps;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStatic;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStorage;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public enum GAMEMODESENUM {
    CREATIVE(1, GameMode.CREATIVE),
    SURVIVAL(0, GameMode.SURVIVAL),
    ADVENTURE(2, GameMode.ADVENTURE),
    SPECTATOR(3, GameMode.SPECTATOR),
    GHOSTMODE(4, GameMode.ADVENTURE);
    private final int id;
    private final GameMode fallback;

    private static final Map<Integer, GAMEMODESENUM> BY_ID = Maps.newHashMap(); // Required: enum-level lookup + persistent storage state
    private static final String gmTable; // NOSONAR: initialized in static block
    private static final RPStorage storage;

    // ⚡ Bolt: Cache ghost mode states in memory to prevent synchronized disk lookups during high-frequency events (like PlayerMoveEvent)
    private static final Set<UUID> GHOST_CACHE = ConcurrentHashMap.newKeySet();

    GAMEMODESENUM(final int id, GameMode def) {
        this.id = id;
        this.fallback = def;
    }

    public static GAMEMODESENUM gmCast(GameMode gm) {
        return BY_ID.get(gm.getValue());
    }
    public static GameMode gmCast(GAMEMODESENUM gm) {
        return gm.fallback;
    }
    public static GAMEMODESENUM getPlayerGameMode(Player player) {
        if (GHOST_CACHE.contains(player.getUniqueId())) {
            return GAMEMODESENUM.GHOSTMODE;
        }
        return gmCast(player.getGameMode());
    }
    public static byte setPlayerGameMode(Player player, GAMEMODESENUM gm) {
        player.setGameMode(gm.fallback);

        boolean invul = gm.isInvulnerable();
        boolean ghom = gm == GAMEMODESENUM.GHOSTMODE;
        player.setCanPickupItems(!ghom); // settings
        player.setAllowFlight(invul);
        player.setCollidable(!invul);
        player.setInvisible(ghom);
        player.setInvulnerable(invul);
        if (ghom) {
            player.setViewDistance(2);
            player.setPlayerWeather(WeatherType.DOWNFALL);
            player.playSound(player, Sound.AMBIENT_CAVE, 2, 3);
            player.sendPotionEffectChange(player, new PotionEffect(PotionEffectType.DARKNESS, 60, 0, false));
        } else {
            player.resetPlayerWeather();
            player.setViewDistance(player.getWorld().getViewDistance());
        }

        String uuid = player.getUniqueId().toString();
        return switch(gm) {
            case GHOSTMODE -> {
                GHOST_CACHE.add(player.getUniqueId());
                if (storage.setValueIfChanged(gmTable, uuid, player.getName())) {
                    storage.saveConfig();
                }
                yield storage.hasValue(gmTable, uuid) ? (byte)1 : (byte)0;
            }
            default -> {
                GHOST_CACHE.remove(player.getUniqueId());
                if (storage.hasValue(gmTable, uuid)) {
                    storage.setValue(gmTable, uuid, null);
                    storage.saveConfig();
                }
                yield player.getGameMode() == gm.fallback ? (byte) 1 : (byte) 0;
            }
        };
    }
    public static byte setPlayerGameMode(Player player, GameMode gm) {
        return setPlayerGameMode(player, gmCast(gm));
    }
    public static void updatePlayerModifiers(Player player) {
        GAMEMODESENUM gm = getPlayerGameMode(player);
        boolean invul = gm.isInvulnerable();
        boolean ghom = gm == GAMEMODESENUM.GHOSTMODE;
        player.setCanPickupItems(!ghom); // settings
        player.setAllowFlight(invul);
        player.setCollidable(!invul);
        player.setInvisible(ghom);
        player.setInvulnerable(invul);
        if (ghom) {
            player.setPlayerWeather(WeatherType.DOWNFALL);
        } else {
            player.resetPlayerWeather();
        }
    }

    public GameMode toGameMode() {
        return gmCast(this);
    }
    public int getGameModeID() {
        return id;
    }
    public GameMode getGameModeFallback() {
        return fallback;
    }
    public boolean isInvulnerable() {
        return this.fallback.isInvulnerable() || this == GHOSTMODE;
    }

    static {
        gmTable = "ghostmodeplayers";
        storage = new RPStorage(RPStatic.CLIENT, gmTable + ".yml");
        for (GAMEMODESENUM mode : values()) {
            BY_ID.put(mode.id, mode);
        }
        try {
            Map<String, Object> tableData = storage.getTable(gmTable);
            for (String key : tableData.keySet()) {
                try {
                    GHOST_CACHE.add(UUID.fromString(key));
                } catch (IllegalArgumentException ignored) {}
            }
        } catch (NullPointerException ignored) {
            // Table doesn't exist yet
        }
    }
}
