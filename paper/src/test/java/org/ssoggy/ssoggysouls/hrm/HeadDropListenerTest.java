package org.ssoggy.ssoggysouls.hrm;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import org.ssoggy.ssoggysouls.SSoggySouls;
import org.ssoggy.ssoggysouls.database.DatabaseManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class HeadDropListenerTest {

    private MockedStatic<Bukkit> mockedBukkit;
    private SSoggySouls plugin;
    private DatabaseManager db;
    private ItemFactory itemFactory;
    private HeadDropListener listener;

    @BeforeEach
    void setUp() {
        mockedBukkit = mockStatic(Bukkit.class);
        itemFactory = mock(ItemFactory.class);
        mockedBukkit.when(Bukkit::getItemFactory).thenReturn(itemFactory);

        SkullMeta mockSkullMeta = mock(SkullMeta.class);
        when(itemFactory.getItemMeta(Material.PLAYER_HEAD)).thenReturn(mockSkullMeta);

        plugin = mock(SSoggySouls.class);
        db = mock(DatabaseManager.class);
        when(plugin.getDatabaseManager()).thenReturn(db);

        listener = new HeadDropListener(plugin);
    }

    @AfterEach
    void tearDown() {
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
    }

    @Test
    void testOnPlayerDeath_bypassed() {
        Player player = mock(Player.class);
        when(player.hasPermission("ssoggysouls.bypass")).thenReturn(true);

        PlayerDeathEvent event = mock(PlayerDeathEvent.class);
        when(event.getEntity()).thenReturn(player);

        listener.onPlayerDeath(event);

        verify(player, never()).getLocation();
    }

    @Test
    void testOnPlayerDeath_normalDeath_dropsHeadsEnabled() {
        Player player = mock(Player.class);
        UUID uuid = UUID.randomUUID();
        World world = mock(World.class);
        Location deathLoc = new Location(world, 100, 64, 200);

        when(player.hasPermission("ssoggysouls.bypass")).thenReturn(false);
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getWorld()).thenReturn(world);
        when(player.getLocation()).thenReturn(deathLoc);
        when(world.getMinHeight()).thenReturn(-64);
        when(world.getName()).thenReturn("world");

        when(plugin.isHrmDeathLocationMsg()).thenReturn(true);
        when(plugin.isHrmDropHeads()).thenReturn(true);

        PlayerDeathEvent event = mock(PlayerDeathEvent.class);
        when(event.getEntity()).thenReturn(player);

        listener.onPlayerDeath(event);

        verify(player).sendRichMessage(anyString());

        // Verify pending death resolved call drops item
        when(plugin.isHrmHeadPlaceAsBlock()).thenReturn(false);
        when(plugin.isHrmHeadFireproof()).thenReturn(true);
        when(plugin.isDebugMode()).thenReturn(false);

        Item mockItem = mock(Item.class);
        when(world.dropItemNaturally(eq(deathLoc), any(ItemStack.class))).thenReturn(mockItem);

        listener.onDeathResolved(player, true);

        verify(world).dropItemNaturally(eq(deathLoc), any(ItemStack.class));
        verify(mockItem).setInvulnerable(true);
    }

    @Test
    void testOnDeathResolved_notFinalDeath() {
        Player player = mock(Player.class);
        UUID uuid = UUID.randomUUID();
        World world = mock(World.class);
        Location deathLoc = new Location(world, 10, 20, 30);

        when(player.hasPermission("ssoggysouls.bypass")).thenReturn(false);
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getWorld()).thenReturn(world);
        when(player.getLocation()).thenReturn(deathLoc);
        when(world.getMinHeight()).thenReturn(-64);

        when(plugin.isHrmDropHeads()).thenReturn(true);

        PlayerDeathEvent event = mock(PlayerDeathEvent.class);
        when(event.getEntity()).thenReturn(player);

        listener.onPlayerDeath(event);

        listener.onDeathResolved(player, false);

        verify(world, never()).dropItemNaturally(any(), any());
    }

    @Test
    void testOnDeathResolved_placeHeadAsBlock() {
        Player player = mock(Player.class);
        UUID uuid = UUID.randomUUID();
        World world = mock(World.class);
        Location deathLoc = new Location(world, 100, 64, 200);

        when(player.hasPermission("ssoggysouls.bypass")).thenReturn(false);
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getWorld()).thenReturn(world);
        when(player.getLocation()).thenReturn(deathLoc);
        when(world.getMinHeight()).thenReturn(-64);
        when(world.getMaxHeight()).thenReturn(320);

        Block mockAirBlock = mock(Block.class);
        Block mockBelowBlock = mock(Block.class);
        when(mockAirBlock.getType()).thenReturn(Material.AIR);
        when(mockAirBlock.getX()).thenReturn(100);
        when(mockAirBlock.getY()).thenReturn(64);
        when(mockAirBlock.getZ()).thenReturn(200);
        when(mockAirBlock.getLocation()).thenReturn(deathLoc);
        when(mockBelowBlock.getType()).thenReturn(Material.STONE);
        when(mockBelowBlock.getType().isSolid()).thenReturn(true);

        when(world.getBlockAt(100, 64, 200)).thenReturn(mockAirBlock);
        when(world.getBlockAt(100, 63, 200)).thenReturn(mockBelowBlock);

        Skull mockSkull = mock(Skull.class);
        when(mockAirBlock.getState()).thenReturn(mockSkull);

        when(plugin.isHrmDropHeads()).thenReturn(true);
        when(plugin.isHrmHeadPlaceAsBlock()).thenReturn(true);

        PlayerDeathEvent event = mock(PlayerDeathEvent.class);
        when(event.getEntity()).thenReturn(player);

        listener.onPlayerDeath(event);
        listener.onDeathResolved(player, true);

        verify(mockAirBlock).setType(Material.PLAYER_HEAD, false);
        verify(mockSkull).setOwningPlayer(player);
        verify(mockSkull).update(true, false);
    }

    @Test
    void testOnItemDespawn_disabled() {
        when(plugin.isHrmDropHeads()).thenReturn(true);
        when(plugin.isHrmHeadPlaceAsBlock()).thenReturn(false);
        when(plugin.isHrmHeadNoDespawn()).thenReturn(false);

        ItemDespawnEvent event = mock(ItemDespawnEvent.class);
        listener.onItemDespawn(event);

        verify(event, never()).setCancelled(anyBoolean());
    }

    @Test
    void testOnItemDespawn_notHeadItem() {
        when(plugin.isHrmDropHeads()).thenReturn(true);
        when(plugin.isHrmHeadPlaceAsBlock()).thenReturn(false);
        when(plugin.isHrmHeadNoDespawn()).thenReturn(true);

        Item mockItemEntity = mock(Item.class);
        ItemStack mockStack = mock(ItemStack.class);
        when(mockStack.getType()).thenReturn(Material.DIRT);
        when(mockItemEntity.getItemStack()).thenReturn(mockStack);

        ItemDespawnEvent event = mock(ItemDespawnEvent.class);
        when(event.getEntity()).thenReturn(mockItemEntity);

        listener.onItemDespawn(event);

        verify(event, never()).setCancelled(true);
    }

    @Test
    void testOnItemDespawn_validHeadItem() {
        when(plugin.isHrmDropHeads()).thenReturn(true);
        when(plugin.isHrmHeadPlaceAsBlock()).thenReturn(false);
        when(plugin.isHrmHeadNoDespawn()).thenReturn(true);

        Item mockItemEntity = mock(Item.class);
        ItemStack mockStack = mock(ItemStack.class);
        SkullMeta mockMeta = mock(SkullMeta.class);
        OfflinePlayer mockOwner = mock(OfflinePlayer.class);
        UUID ownerUuid = UUID.randomUUID();

        when(mockStack.getType()).thenReturn(Material.PLAYER_HEAD);
        when(mockStack.getItemMeta()).thenReturn(mockMeta);
        when(mockMeta.getOwningPlayer()).thenReturn(mockOwner);
        when(mockOwner.getUniqueId()).thenReturn(ownerUuid);
        when(mockItemEntity.getItemStack()).thenReturn(mockStack);

        ItemDespawnEvent event = mock(ItemDespawnEvent.class);
        when(event.getEntity()).thenReturn(mockItemEntity);

        org.bukkit.scheduler.BukkitScheduler scheduler = mock(org.bukkit.scheduler.BukkitScheduler.class);
        mockedBukkit.when(Bukkit::getScheduler).thenReturn(scheduler);

        listener.onItemDespawn(event);

        verify(event).setCancelled(true);
        verify(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));
    }

    @Test
    void testCreatePlayerHead() {
        Player player = mock(Player.class);
        when(player.getName()).thenReturn("TestPlayer");

        SkullMeta mockMeta = mock(SkullMeta.class);
        when(itemFactory.getItemMeta(Material.PLAYER_HEAD)).thenReturn(mockMeta);

        ItemStack stack = HeadDropListener.createPlayerHead(player);

        assertNotNull(stack);
        assertEquals(Material.PLAYER_HEAD, stack.getType());
        verify(mockMeta).setOwningPlayer(player);
        verify(mockMeta).displayName(any());
    }

    @Test
    void testRemoveDroppedHeads() {
        UUID ownerUuid = UUID.randomUUID();
        org.bukkit.scheduler.BukkitScheduler scheduler = mock(org.bukkit.scheduler.BukkitScheduler.class);
        mockedBukkit.when(Bukkit::getScheduler).thenReturn(scheduler);

        when(plugin.isHrmHeadPlaceAsBlock()).thenReturn(true);

        listener.removeDroppedHeads(ownerUuid);

        verify(scheduler).runTaskTimer(eq(plugin), any(Runnable.class), eq(0L), eq(1L));
    }
}
