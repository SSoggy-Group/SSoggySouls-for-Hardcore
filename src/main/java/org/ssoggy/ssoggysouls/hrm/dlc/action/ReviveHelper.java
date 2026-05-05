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
import java.util.List;
import java.util.Set;

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

    private record RitualPatternEntry(int dx, int dy, int dz, String tag, Set<Material> fixedMaterials) {
        public Set<Material> getMaterials() {
            if (fixedMaterials != null) return fixedMaterials;
            return RPStatic.BLOCK_TAGS.getOrDefault(tag, Collections.emptySet());
        }
    }

    private static final List<RitualPatternEntry> FULL_RITUAL_PATTERN = List.of(
            new RitualPatternEntry(0, 0, 0, null, Set.of(Material.PLAYER_HEAD)),
            new RitualPatternEntry(0, -1, 0, "fence-blocktag", null),
            new RitualPatternEntry(0, -1, -1, TAG_FLOWER, null),
            new RitualPatternEntry(0, -1, 1, TAG_FLOWER, null),
            new RitualPatternEntry(-1, -1, 0, TAG_FLOWER, null),
            new RitualPatternEntry(1, -1, 0, TAG_FLOWER, null),
            new RitualPatternEntry(0, -2, 0, "ore-blocktag", null),
            new RitualPatternEntry(0, -2, 2, TAG_STAIR, null),
            new RitualPatternEntry(0, -2, -2, TAG_STAIR, null),
            new RitualPatternEntry(2, -2, 0, TAG_STAIR, null),
            new RitualPatternEntry(-2, -2, 0, TAG_STAIR, null),
            new RitualPatternEntry(0, -2, 1, TAG_SOUL_SAND, null),
            new RitualPatternEntry(0, -2, -1, TAG_SOUL_SAND, null),
            new RitualPatternEntry(1, -2, 0, TAG_SOUL_SAND, null),
            new RitualPatternEntry(-1, -2, 0, TAG_SOUL_SAND, null),
            new RitualPatternEntry(-1, -2, -1, TAG_SOUL_SAND, null),
            new RitualPatternEntry(-1, -2, 1, TAG_SOUL_SAND, null),
            new RitualPatternEntry(1, -2, -1, TAG_SOUL_SAND, null),
            new RitualPatternEntry(1, -2, 1, TAG_SOUL_SAND, null)
    );

    private static final List<RitualPatternEntry> FAILED_RITUAL_PATTERN = List.of(
            new RitualPatternEntry(0, 0, 0, null, Set.of(Material.PLAYER_HEAD)),
            new RitualPatternEntry(0, -1, 0, "fence-blocktag", null),
            new RitualPatternEntry(0, -2, 0, "ore-blocktag", null),
            new RitualPatternEntry(0, -2, 2, TAG_STAIR, null),
            new RitualPatternEntry(0, -2, -2, TAG_STAIR, null),
            new RitualPatternEntry(2, -2, 0, TAG_STAIR, null),
            new RitualPatternEntry(-2, -2, 0, TAG_STAIR, null),
            new RitualPatternEntry(0, -2, 1, TAG_SOUL_SAND, null),
            new RitualPatternEntry(0, -2, -1, TAG_SOUL_SAND, null),
            new RitualPatternEntry(1, -2, 0, TAG_SOUL_SAND, null),
            new RitualPatternEntry(-1, -2, 0, TAG_SOUL_SAND, null),
            new RitualPatternEntry(-1, -2, -1, TAG_SOUL_SAND, null),
            new RitualPatternEntry(-1, -2, 1, TAG_SOUL_SAND, null),
            new RitualPatternEntry(1, -2, -1, TAG_SOUL_SAND, null),
            new RitualPatternEntry(1, -2, 1, TAG_SOUL_SAND, null)
    );

    private ReviveHelper() {}

    private static boolean matchesRitualPattern(World world, Location pos, List<RitualPatternEntry> pattern) {
        for (RitualPatternEntry entry : pattern) {
            int targetX = pos.getBlockX() + entry.dx();
            int targetY = pos.getBlockY() + entry.dy();
            int targetZ = pos.getBlockZ() + entry.dz();

            if (targetY <= world.getMinHeight()) {
                return false;
            }

            Material blockType = world.getBlockAt(targetX, targetY, targetZ).getType();
            if (!entry.getMaterials().contains(blockType)) {
                return false;
            }
        }
        return true;
    }

    private static void removeRitualPattern(World world, Location pos, List<RitualPatternEntry> pattern) {
        boolean keepBase = Boolean.TRUE.equals(RPStatic.CONFIG_RULES.getOrDefault("keep-structure-base", false));

        for (RitualPatternEntry entry : pattern) {
            // Skip the base layer (dy = -2) if keep-structure-base is enabled
            if (keepBase && entry.dy() == -2) continue;

            int targetX = pos.getBlockX() + entry.dx();
            int targetY = pos.getBlockY() + entry.dy();
            int targetZ = pos.getBlockZ() + entry.dz();

            if (targetY > world.getMinHeight() && targetY <= pos.getBlockY()) {
                world.getBlockAt(targetX, targetY, targetZ).setType(Material.AIR);
            }
        }

        world.playSound(pos, Sound.BLOCK_STONE_BREAK, 0.4F, 1.0F);
    }

    public static boolean tryRevivePlayer(World world, Location pos, Player deadPlayer, Player alivePlayer) {
        RPStats aliveStats = new RPStats(alivePlayer.getUniqueId());
        aliveStats.incrementStat(STATSENUM.RITUAL_STARTED, 1);

        if (!matchesRitualPattern(world, pos, FULL_RITUAL_PATTERN)) {
            if (matchesRitualPattern(world, pos, FAILED_RITUAL_PATTERN)) {
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
        spawnPlayer(world, pos, deadPlayer, alivePlayer);
        return true;
    }

    private static void spawnPlayer(World world, Location pos, Player deadPlayer, Player alivePlayer) {
        removeRitualPattern(world, pos, FULL_RITUAL_PATTERN);

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
}
