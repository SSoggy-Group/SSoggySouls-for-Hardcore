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

package org.ssoggy.ssoggysouls.hrm.dlc.action;

import io.papermc.paper.entity.LookAnchor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.ssoggy.ssoggysouls.hrm.dlc.enums.STATSENUM;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStatic;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStats;
import net.kyori.adventure.text.Component;
import org.bukkit.EntityEffect;
import org.ssoggy.ssoggysouls.hrm.dlc.enums.GAMEMODESENUM;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ReviveHelper {
    private static final String TAG_FLOWER = "flower-blocktag";
    private static final String TAG_STAIR = "stair-blocktag";
    private static final String TAG_SOUL_SAND = "soul-sand-blocktag";

    private ReviveHelper() {}

    public static boolean tryRevivePlayer(World world, Location pos, Player deadPlayer, Player alivePlayer) {
        RPStats aliveStats = new RPStats(alivePlayer.getUniqueId());
        aliveStats.incrementStat(STATSENUM.RITUAL_STARTED, 1);

        Map<Location, Set<Material>> result = searchAround(world, pos, getRitualPattern(world));
        if (result.isEmpty()) {
            Map<Location, Set<Material>> incompleteResult = searchAround(world, pos, getFailedRitualPattern(world));
            if (!incompleteResult.isEmpty()) {
                spawnError("Something is missing...", world, pos, alivePlayer);
            }
            return false;
        }

        if (deadPlayer == null) {
            spawnError("It appears that this player is absent to this world", world, pos, alivePlayer);
            return false;
        }

        if (GAMEMODESENUM.getPlayerGameMode(deadPlayer) != GAMEMODESENUM.GHOSTMODE) {
            spawnError("It appears that this player is still alive", world, pos, alivePlayer);

            if (deadPlayer == alivePlayer) {
                spawnError("Oh! That's you!", world, pos, alivePlayer);
                return false;
            }
            return false;
        }

        if (!deadPlayer.isOnline()) {
            spawnError("It appears that this player is absent to this world", world, pos, alivePlayer);
            return false;
        }


        RPStats deadStats = new RPStats(deadPlayer.getUniqueId());
        aliveStats.incrementStat(STATSENUM.RITUAL_COMPLETED, 1);
        deadStats.incrementStat(STATSENUM.REVIVES, 1);

        if (Boolean.TRUE.equals(RPStatic.CONFIG_RULES.getOrDefault("ritual-lightning-strike", true))) {
            world.strikeLightning(new Location(world, pos.getBlockX() + 0.5, (double) pos.getBlockY() - 1, pos.getBlockZ() - 0.5));
        }
        spawnPlayer(world, pos, result, deadPlayer, alivePlayer);
        return true;
    }

    private static void spawnPlayer(World world, Location pos, Map<Location, Set<Material>> patternResult, Player deadPlayer, Player alivePlayer) {
        breakRitualPatternBlocks(world, pos, patternResult);

        deadPlayer.teleport(new Location(world, pos.getBlockX() + 0.5, pos.getBlockY() - 0.95, pos.getBlockZ() + 0.5));
        deadPlayer.lookAt(alivePlayer, LookAnchor.FEET, LookAnchor.FEET);
        GAMEMODESENUM.setPlayerGameMode(deadPlayer, GAMEMODESENUM.SURVIVAL);

        deadPlayer.clearActivePotionEffects();
        
        int resistanceTicks = RPStatic.CONFIG_TIMERS.getOrDefault("revive-resistance-ticks", 100);
        if (resistanceTicks > 0) {
            deadPlayer.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, resistanceTicks, 70, false, false));
        }
        
        int glowingTicks = RPStatic.CONFIG_TIMERS.getOrDefault("revive-glowing-ticks", 100);
        if (glowingTicks > 0) {
            deadPlayer.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, glowingTicks, 1));
        }

        if (Boolean.TRUE.equals(RPStatic.CONFIG_RULES.getOrDefault("ritual-totem-effect", true))) {
            deadPlayer.sendEntityEffect(EntityEffect.TOTEM_RESURRECT, deadPlayer); // If this is removed in the newer versions then I will cry
        }
    }

    private static void spawnError(String errorMessage, World world, Location pos, Player alivePlayer) {
        alivePlayer.sendActionBar(Component.text(errorMessage));
        world.playSound(pos, Sound.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.4F, 20.0F);
        world.spawnParticle(Particle.SMOKE, new Location(world, pos.getBlockX() + 0.5, pos.getBlockY() + 0.5, pos.getBlockZ() + 0.5), 1);
    }

    private static Map<Location, Set<Material>> searchAround(World world, Location pos, Map<Location, Set<Material>> blockOffsetMap) {
        for(Map.Entry<Location, Set<Material>> offset : blockOffsetMap.entrySet()) {
            Location offsetKey = offset.getKey();
            Location offsetPos = new Location(world, (double) pos.getBlockX() + offsetKey.getBlockX(), (double) pos.getBlockY() + offsetKey.getBlockY(), (double) pos.getBlockZ() + offsetKey.getBlockZ());
            Stream<Material> blockTag = offset.getValue().stream();
            if (offsetPos.getY() <= world.getMinHeight()) {
                return Collections.emptyMap();
            }

            Block block = world.getBlockAt(offsetPos.toBlockLocation());
            if (!offset.getValue().contains(block.getType())) {
                return Collections.emptyMap();
            }
        }

        return blockOffsetMap;
    }

    private static void breakRitualPatternBlocks(World world, Location pos, Map<Location, Set<Material>> patternResult) {
        int deadzoneY = Boolean.TRUE.equals(RPStatic.CONFIG_RULES.getOrDefault("keep-structure-base", false)) ? 1 : 0;

        for(Map.Entry<Location, Set<Material>> offset : patternResult.entrySet()) {
            Location offsetKey = offset.getKey();
            Location offsetPos = new Location(world, (double) pos.getBlockX() + offsetKey.getBlockX(), (double) pos.getBlockY() + offsetKey.getBlockY() + deadzoneY, (double) pos.getBlockZ() + offsetKey.getBlockZ());
            if (offsetPos.getY() > world.getMinHeight() && offsetPos.getBlockY() <= pos.getBlockY()) {
                Block block = world.getBlockAt(offsetPos);
                block.setType(Material.AIR);
            }
        }

        world.playSound(pos, Sound.BLOCK_STONE_BREAK, 0.4F, 1.0F);
    }

    private static Map<Location, Set<Material>> getRitualPattern(World world) {
        Map<Location, Set<Material>> patternMap = new HashMap<>();
        patternMap.put(new Location(world, 0.0F, 0.0F, 0.0F), Set.of(Material.PLAYER_HEAD));
        patternMap.put(new Location(world, 0.0F, -1.0F, 0.0F), RPStatic.BLOCK_TAGS.getOrDefault("fence-blocktag", Set.of()));
        patternMap.put(new Location(world, 0.0F, -1.0F, -1.0F), RPStatic.BLOCK_TAGS.getOrDefault(TAG_FLOWER, Set.of()));
        patternMap.put(new Location(world, 0.0F, -1.0F, 1.0F), RPStatic.BLOCK_TAGS.getOrDefault(TAG_FLOWER, Set.of()));
        patternMap.put(new Location(world, -1.0F, -1.0F, 0.0F), RPStatic.BLOCK_TAGS.getOrDefault(TAG_FLOWER, Set.of()));
        patternMap.put(new Location(world, 1.0F, -1.0F, 0.0F), RPStatic.BLOCK_TAGS.getOrDefault(TAG_FLOWER, Set.of()));
        patternMap.put(new Location(world, 0.0F, -2.0F, 0.0F), RPStatic.BLOCK_TAGS.getOrDefault("ore-blocktag", Set.of()));
        patternMap.put(new Location(world, 0.0F, -2.0F, 2.0F), RPStatic.BLOCK_TAGS.getOrDefault(TAG_STAIR, Set.of()));
        patternMap.put(new Location(world, 0.0F, -2.0F, -2.0F), RPStatic.BLOCK_TAGS.getOrDefault(TAG_STAIR, Set.of()));
        patternMap.put(new Location(world, 2.0F, -2.0F, 0.0F), RPStatic.BLOCK_TAGS.getOrDefault(TAG_STAIR, Set.of()));
        patternMap.put(new Location(world, -2.0F, -2.0F, 0.0F), RPStatic.BLOCK_TAGS.getOrDefault(TAG_STAIR, Set.of()));
        patternMap.put(new Location(world, 0.0F, -2.0F, 1.0F), RPStatic.BLOCK_TAGS.getOrDefault(TAG_SOUL_SAND, Set.of()));
        patternMap.put(new Location(world, 0.0F, -2.0F, -1.0F), RPStatic.BLOCK_TAGS.getOrDefault(TAG_SOUL_SAND, Set.of()));
        patternMap.put(new Location(world, 1.0F, -2.0F, 0.0F), RPStatic.BLOCK_TAGS.getOrDefault(TAG_SOUL_SAND, Set.of()));
        patternMap.put(new Location(world, -1.0F, -2.0F, 0.0F), RPStatic.BLOCK_TAGS.getOrDefault(TAG_SOUL_SAND, Set.of()));
        patternMap.put(new Location(world, -1.0F, -2.0F, -1.0F), RPStatic.BLOCK_TAGS.getOrDefault(TAG_SOUL_SAND, Set.of()));
        patternMap.put(new Location(world, -1.0F, -2.0F, 1.0F), RPStatic.BLOCK_TAGS.getOrDefault(TAG_SOUL_SAND, Set.of()));
        patternMap.put(new Location(world, 1.0F, -2.0F, -1.0F), RPStatic.BLOCK_TAGS.getOrDefault(TAG_SOUL_SAND, Set.of()));
        patternMap.put(new Location(world, 1.0F, -2.0F, 1.0F), RPStatic.BLOCK_TAGS.getOrDefault(TAG_SOUL_SAND, Set.of()));
        return patternMap;
    }

    private static Map<Location, Set<Material>> getFailedRitualPattern(World world) {
        Map<Location, Set<Material>> patternMap = new HashMap<>();
        patternMap.put(new Location(world, 0.0F, 0.0F, 0.0F), Set.of(Material.PLAYER_HEAD));
        patternMap.put(new Location(world, 0.0F, -1.0F, 0.0F), RPStatic.BLOCK_TAGS.getOrDefault("fence-blocktag", Set.of()));
        patternMap.put(new Location(world, 0.0F, -2.0F, 0.0F), RPStatic.BLOCK_TAGS.getOrDefault("ore-blocktag", Set.of()));
        patternMap.put(new Location(world, 0.0F, -2.0F, 2.0F), RPStatic.BLOCK_TAGS.getOrDefault(TAG_STAIR, Set.of()));
        patternMap.put(new Location(world, 0.0F, -2.0F, -2.0F), RPStatic.BLOCK_TAGS.getOrDefault(TAG_STAIR, Set.of()));
        patternMap.put(new Location(world, 2.0F, -2.0F, 0.0F), RPStatic.BLOCK_TAGS.getOrDefault(TAG_STAIR, Set.of()));
        patternMap.put(new Location(world, -2.0F, -2.0F, 0.0F), RPStatic.BLOCK_TAGS.getOrDefault(TAG_STAIR, Set.of()));
        patternMap.put(new Location(world, 0.0F, -2.0F, 1.0F), RPStatic.BLOCK_TAGS.getOrDefault(TAG_SOUL_SAND, Set.of()));
        patternMap.put(new Location(world, 0.0F, -2.0F, -1.0F), RPStatic.BLOCK_TAGS.getOrDefault(TAG_SOUL_SAND, Set.of()));
        patternMap.put(new Location(world, 1.0F, -2.0F, 0.0F), RPStatic.BLOCK_TAGS.getOrDefault(TAG_SOUL_SAND, Set.of()));
        patternMap.put(new Location(world, -1.0F, -2.0F, 0.0F), RPStatic.BLOCK_TAGS.getOrDefault(TAG_SOUL_SAND, Set.of()));
        patternMap.put(new Location(world, -1.0F, -2.0F, -1.0F), RPStatic.BLOCK_TAGS.getOrDefault(TAG_SOUL_SAND, Set.of()));
        patternMap.put(new Location(world, -1.0F, -2.0F, 1.0F), RPStatic.BLOCK_TAGS.getOrDefault(TAG_SOUL_SAND, Set.of()));
        patternMap.put(new Location(world, 1.0F, -2.0F, -1.0F), RPStatic.BLOCK_TAGS.getOrDefault(TAG_SOUL_SAND, Set.of()));
        patternMap.put(new Location(world, 1.0F, -2.0F, 1.0F), RPStatic.BLOCK_TAGS.getOrDefault(TAG_SOUL_SAND, Set.of()));
        return patternMap;
    }
}
