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
    private static final java.util.Set<java.util.UUID> GHOST_PLAYERS = java.util.concurrent.ConcurrentHashMap.newKeySet();


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
        if (GHOST_PLAYERS.contains(player.getUniqueId())) {
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

        java.util.UUID playerUuid = player.getUniqueId();
        String uuidStr = playerUuid.toString();
        return switch(gm) {
            case GHOSTMODE -> {
                GHOST_PLAYERS.add(playerUuid);
                if (storage.setValueIfChanged(gmTable, uuidStr, player.getName())) {
                    storage.saveConfig();
                }
                yield (byte) 1;
            }
            default -> {
                GHOST_PLAYERS.remove(playerUuid);
                if (storage.hasValue(gmTable, uuidStr)) {
                    storage.removeValue(gmTable, uuidStr);
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
            Map<String, Object> table = storage.getTable(gmTable);
            if (table != null) {
                for (String key : table.keySet()) {
                    try {
                        GHOST_PLAYERS.add(java.util.UUID.fromString(key));
                    } catch (IllegalArgumentException ignored) {
                        // ignored: not a valid UUID string in config
                    }
                }
            }
        } catch (Exception ignored) {
            // section might not exist yet
        }
    }
}
