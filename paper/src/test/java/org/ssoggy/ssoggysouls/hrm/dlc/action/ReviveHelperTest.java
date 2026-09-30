package org.ssoggy.ssoggysouls.hrm.dlc.action;

import net.kyori.adventure.text.Component;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ssoggy.ssoggysouls.hrm.dlc.enums.GAMEMODESENUM;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStatic;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStorage;

import java.io.File;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviveHelperTest {

    private World world;
    private Location pos;
    private Player alivePlayer;
    private Player deadPlayer;

    @BeforeEach
    void setUp() {
        File dataDir = new File("build/tmp/test_revive_helper");
        dataDir.mkdirs();

        JavaPlugin mockPlugin = mock(JavaPlugin.class);
        when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("test"));
        when(mockPlugin.getDataFolder()).thenReturn(dataDir);
        RPStatic.CLIENT = mockPlugin;
        RPStatic.STATS_STORAGE = new RPStorage(mockPlugin, "stats.yml");

        RPStatic.BLOCK_TAGS = new HashMap<>();
        RPStatic.CONFIG_RULES = new HashMap<>();
        RPStatic.CONFIG_TIMERS = new HashMap<>();

        RPStatic.BLOCK_TAGS.put("fence-blocktag", Set.of(Material.OAK_FENCE));
        RPStatic.BLOCK_TAGS.put("flower-blocktag", Set.of(Material.DANDELION));
        RPStatic.BLOCK_TAGS.put("ore-blocktag", Set.of(Material.GOLD_ORE));
        RPStatic.BLOCK_TAGS.put("stair-blocktag", Set.of(Material.OAK_STAIRS));
        RPStatic.BLOCK_TAGS.put("soul-sand-blocktag", Set.of(Material.SOUL_SAND));

        world = mock(World.class);
        when(world.getMinHeight()).thenReturn(-64);

        pos = new Location(world, 10, 64, 10);

        alivePlayer = mock(Player.class);
        UUID aliveUuid = UUID.randomUUID();
        when(alivePlayer.getUniqueId()).thenReturn(aliveUuid);
        when(alivePlayer.getGameMode()).thenReturn(GameMode.SURVIVAL);
        when(alivePlayer.isOnline()).thenReturn(true);
        when(alivePlayer.getWorld()).thenReturn(world);

        deadPlayer = mock(Player.class);
        UUID deadUuid = UUID.randomUUID();
        when(deadPlayer.getUniqueId()).thenReturn(deadUuid);
        when(deadPlayer.getGameMode()).thenReturn(GameMode.ADVENTURE);
        when(deadPlayer.isOnline()).thenReturn(true);
        when(deadPlayer.getWorld()).thenReturn(world);

        GAMEMODESENUM.setPlayerGameMode(deadPlayer, GAMEMODESENUM.GHOSTMODE);
    }

    private void setupMockBlocks(boolean fullPattern, boolean failedPattern) {
        Block headBlock = mock(Block.class);
        when(headBlock.getType()).thenReturn(Material.PLAYER_HEAD);

        Block fenceBlock = mock(Block.class);
        when(fenceBlock.getType()).thenReturn(Material.OAK_FENCE);

        Block flowerBlock = mock(Block.class);
        when(flowerBlock.getType()).thenReturn(fullPattern ? Material.DANDELION : Material.AIR);

        Block oreBlock = mock(Block.class);
        when(oreBlock.getType()).thenReturn((fullPattern || failedPattern) ? Material.GOLD_ORE : Material.AIR);

        Block stairBlock = mock(Block.class);
        when(stairBlock.getType()).thenReturn((fullPattern || failedPattern) ? Material.OAK_STAIRS : Material.AIR);

        Block soulSandBlock = mock(Block.class);
        when(soulSandBlock.getType()).thenReturn((fullPattern || failedPattern) ? Material.SOUL_SAND : Material.AIR);

        when(world.getBlockAt(any(Integer.class), any(Integer.class), any(Integer.class))).thenAnswer(invocation -> {
            int x = invocation.getArgument(0);
            int y = invocation.getArgument(1);
            int z = invocation.getArgument(2);

            int dx = x - pos.getBlockX();
            int dy = y - pos.getBlockY();
            int dz = z - pos.getBlockZ();

            if (dx == 0 && dy == 0 && dz == 0) return headBlock;
            if (dx == 0 && dy == -1 && dz == 0) return fenceBlock;
            if (dy == -1 && (dx != 0 || dz != 0)) return flowerBlock;
            if (dx == 0 && dy == -2 && dz == 0) return oreBlock;
            if (dy == -2 && (Math.abs(dx) == 2 || Math.abs(dz) == 2)) return stairBlock;
            if (dy == -2) return soulSandBlock;

            Block airBlock = mock(Block.class);
            when(airBlock.getType()).thenReturn(Material.AIR);
            return airBlock;
        });
    }

    @Test
    void testTargetBelowMinHeight() {
        when(world.getMinHeight()).thenReturn(64);
        boolean result = ReviveHelper.tryRevivePlayer(world, pos, deadPlayer, alivePlayer);
        assertFalse(result);
    }

    @Test
    void testFailedRitualPatternMatches() {
        setupMockBlocks(false, true);

        boolean result = ReviveHelper.tryRevivePlayer(world, pos, deadPlayer, alivePlayer);

        assertFalse(result);
        verify(alivePlayer).sendActionBar(any(Component.class));
        verify(world).playSound(eq(pos), eq(Sound.BLOCK_FIRE_EXTINGUISH), eq(SoundCategory.BLOCKS), eq(0.4F), eq(20.0F));
        verify(world).spawnParticle(eq(Particle.SMOKE), any(Location.class), eq(1));
    }

    @Test
    void testNoPatternMatches() {
        setupMockBlocks(false, false);

        boolean result = ReviveHelper.tryRevivePlayer(world, pos, deadPlayer, alivePlayer);

        assertFalse(result);
        verify(alivePlayer, never()).sendActionBar(any(Component.class));
    }

    @Test
    void testDeadPlayerNull() {
        setupMockBlocks(true, false);

        boolean result = ReviveHelper.tryRevivePlayer(world, pos, null, alivePlayer);

        assertFalse(result);
        verify(alivePlayer).sendActionBar(any(Component.class));
    }

    @Test
    void testDeadPlayerNotGhostmode() {
        setupMockBlocks(true, false);
        GAMEMODESENUM.setPlayerGameMode(deadPlayer, GAMEMODESENUM.SURVIVAL);

        boolean result = ReviveHelper.tryRevivePlayer(world, pos, deadPlayer, alivePlayer);

        assertFalse(result);
        verify(alivePlayer).sendActionBar(any(Component.class));
    }

    @Test
    void testDeadPlayerIsAlivePlayerNotGhostmode() {
        setupMockBlocks(true, false);
        GAMEMODESENUM.setPlayerGameMode(alivePlayer, GAMEMODESENUM.SURVIVAL);

        boolean result = ReviveHelper.tryRevivePlayer(world, pos, alivePlayer, alivePlayer);

        assertFalse(result);
        verify(alivePlayer, atLeastOnce()).sendActionBar(any(Component.class));
    }

    @Test
    void testDeadPlayerOffline() {
        setupMockBlocks(true, false);
        when(deadPlayer.isOnline()).thenReturn(false);

        boolean result = ReviveHelper.tryRevivePlayer(world, pos, deadPlayer, alivePlayer);

        assertFalse(result);
        verify(alivePlayer).sendActionBar(any(Component.class));
    }

    @Test
    void testSuccessfulRevivalDefaultConfig() {
        setupMockBlocks(true, false);
        RPStatic.CONFIG_RULES.put("ritual-lightning-strike", true);
        RPStatic.CONFIG_RULES.put("ritual-totem-effect", true);
        RPStatic.CONFIG_RULES.put("keep-structure-base", false);
        RPStatic.CONFIG_TIMERS.put("revive-resistance-ticks", 100);
        RPStatic.CONFIG_TIMERS.put("revive-glowing-ticks", 100);

        boolean result = ReviveHelper.tryRevivePlayer(world, pos, deadPlayer, alivePlayer);

        assertTrue(result);
        verify(world).strikeLightning(any(Location.class));
        verify(deadPlayer).teleport(any(Location.class));
        verify(deadPlayer).clearActivePotionEffects();
        verify(deadPlayer).addPotionEffect(any(PotionEffect.class));
        verify(deadPlayer).sendEntityEffect(eq(EntityEffect.TOTEM_RESURRECT), eq(deadPlayer));
    }

    @Test
    void testSuccessfulRevivalDisabledLightningTotemKeepBase() {
        setupMockBlocks(true, false);
        RPStatic.CONFIG_RULES.put("ritual-lightning-strike", false);
        RPStatic.CONFIG_RULES.put("ritual-totem-effect", false);
        RPStatic.CONFIG_RULES.put("keep-structure-base", true);
        RPStatic.CONFIG_TIMERS.put("revive-resistance-ticks", 0);
        RPStatic.CONFIG_TIMERS.put("revive-glowing-ticks", 0);

        boolean result = ReviveHelper.tryRevivePlayer(world, pos, deadPlayer, alivePlayer);

        assertTrue(result);
        verify(world, never()).strikeLightning(any(Location.class));
        verify(deadPlayer, never()).sendEntityEffect(eq(EntityEffect.TOTEM_RESURRECT), eq(deadPlayer));
        verify(deadPlayer, never()).addPotionEffect(any(PotionEffect.class));
    }
}
