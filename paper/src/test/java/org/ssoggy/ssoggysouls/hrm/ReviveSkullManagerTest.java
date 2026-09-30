package org.ssoggy.ssoggysouls.hrm;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.ssoggy.ssoggysouls.SSoggySouls;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.model.PlayerData;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviveSkullManagerTest {

    private MockedStatic<Bukkit> mockedBukkit;
    private SSoggySouls plugin;
    private DatabaseManager dbManager;
    private BukkitScheduler scheduler;
    private ItemFactory itemFactory;
    private ReviveSkullManager manager;

    @BeforeEach
    void setUp() {
        mockedBukkit = mockStatic(Bukkit.class);
        plugin = mock(SSoggySouls.class);
        dbManager = mock(DatabaseManager.class);
        scheduler = mock(BukkitScheduler.class);
        itemFactory = mock(ItemFactory.class);

        when(plugin.getName()).thenReturn("SSoggySouls");
        when(plugin.getDatabaseManager()).thenReturn(dbManager);
        mockedBukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
        mockedBukkit.when(Bukkit::getItemFactory).thenReturn(itemFactory);

        manager = new ReviveSkullManager(plugin);
    }

    @AfterEach
    void tearDown() {
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
    }

    @Test
    void testGetRecipeKey() {
        NamespacedKey key = manager.getRecipeKey();
        assertNotNull(key);
        assertEquals("ssoggysouls", key.getNamespace());
        assertEquals("revive_skull", key.getKey());
    }

    @Test
    void testIsReviveSkull_nullItem() {
        assertFalse(manager.isReviveSkull(null));
    }

    @Test
    void testIsReviveSkull_nonPlayerHeadItem() {
        ItemStack item = mock(ItemStack.class);
        when(item.getType()).thenReturn(Material.DIRT);
        assertFalse(manager.isReviveSkull(item));
    }

    @Test
    void testIsReviveSkull_nullMeta() {
        ItemStack item = mock(ItemStack.class);
        when(item.getType()).thenReturn(Material.PLAYER_HEAD);
        when(item.getItemMeta()).thenReturn(null);
        assertFalse(manager.isReviveSkull(item));
    }

    @Test
    void testIsReviveSkull_withoutTag() {
        ItemStack item = mock(ItemStack.class);
        ItemMeta meta = mock(ItemMeta.class);
        PersistentDataContainer pdc = mock(PersistentDataContainer.class);

        when(item.getType()).thenReturn(Material.PLAYER_HEAD);
        when(item.getItemMeta()).thenReturn(meta);
        when(meta.getPersistentDataContainer()).thenReturn(pdc);
        when(pdc.has(any(NamespacedKey.class), eq(PersistentDataType.BYTE))).thenReturn(false);

        assertFalse(manager.isReviveSkull(item));
    }

    @Test
    void testIsReviveSkull_withValidTag() {
        ItemStack item = mock(ItemStack.class);
        ItemMeta meta = mock(ItemMeta.class);
        PersistentDataContainer pdc = mock(PersistentDataContainer.class);

        when(item.getType()).thenReturn(Material.PLAYER_HEAD);
        when(item.getItemMeta()).thenReturn(meta);
        when(meta.getPersistentDataContainer()).thenReturn(pdc);
        when(pdc.has(any(NamespacedKey.class), eq(PersistentDataType.BYTE))).thenReturn(true);

        assertTrue(manager.isReviveSkull(item));
    }

    @Test
    void testCreateReviveSkullItem() {
        SkullMeta meta = mock(SkullMeta.class);
        PersistentDataContainer pdc = mock(PersistentDataContainer.class);
        when(meta.getPersistentDataContainer()).thenReturn(pdc);
        when(itemFactory.getItemMeta(Material.PLAYER_HEAD)).thenReturn(meta);
        when(itemFactory.isApplicable(any(), any())).thenReturn(true);

        ItemStack skull = manager.createReviveSkullItem();
        assertNotNull(skull);
        assertEquals(Material.PLAYER_HEAD, skull.getType());
        verify(meta).displayName(Component.text("Revive Skull", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD));
        verify(pdc).set(eq(manager.getRecipeKey()), eq(PersistentDataType.BYTE), eq((byte) 1));
    }

    @Test
    void testRegisterRecipe() {
        SkullMeta meta = mock(SkullMeta.class);
        PersistentDataContainer pdc = mock(PersistentDataContainer.class);
        when(meta.getPersistentDataContainer()).thenReturn(pdc);
        when(itemFactory.getItemMeta(Material.PLAYER_HEAD)).thenReturn(meta);

        manager.registerRecipe();

        mockedBukkit.verify(() -> Bukkit.removeRecipe(new NamespacedKey("hardcorelimbo", "revive_skull")));
        mockedBukkit.verify(() -> Bukkit.addRecipe(any(ShapedRecipe.class)));
        verify(plugin).debug("Registered Revive Skull crafting recipe.");
    }

    @Test
    void testUnregisterRecipe() {
        manager.unregisterRecipe();
        mockedBukkit.verify(() -> Bukkit.removeRecipe(manager.getRecipeKey()));
    }

    @Test
    void testOnPlayerJoin() {
        Player player = mock(Player.class);
        PlayerJoinEvent event = new PlayerJoinEvent(player, Component.empty());

        manager.onPlayerJoin(event);

        verify(player).discoverRecipe(manager.getRecipeKey());
    }

    @Test
    void testOnPlayerInteract_ignoredActions() {
        PlayerInteractEvent event = mock(PlayerInteractEvent.class);
        when(event.getAction()).thenReturn(Action.LEFT_CLICK_AIR);

        manager.onPlayerInteract(event);

        verify(event, never()).setCancelled(true);
        verify(scheduler, never()).runTaskAsynchronously(any(SSoggySouls.class), any(Runnable.class));
    }

    @Test
    void testOnPlayerInteract_notReviveSkull() {
        PlayerInteractEvent event = mock(PlayerInteractEvent.class);
        ItemStack item = mock(ItemStack.class);
        when(event.getAction()).thenReturn(Action.RIGHT_CLICK_AIR);
        when(event.getItem()).thenReturn(item);
        when(item.getType()).thenReturn(Material.DIRT);

        manager.onPlayerInteract(event);

        verify(event, never()).setCancelled(true);
        verify(scheduler, never()).runTaskAsynchronously(any(SSoggySouls.class), any(Runnable.class));
    }

    @Test
    void testOnPlayerInteract_validSkullEmptyDeadPlayers() {
        PlayerInteractEvent event = mock(PlayerInteractEvent.class);
        Player player = mock(Player.class);
        ItemStack item = mock(ItemStack.class);
        ItemMeta meta = mock(ItemMeta.class);
        PersistentDataContainer pdc = mock(PersistentDataContainer.class);

        when(event.getAction()).thenReturn(Action.RIGHT_CLICK_AIR);
        when(event.getItem()).thenReturn(item);
        when(event.getPlayer()).thenReturn(player);
        when(item.getType()).thenReturn(Material.PLAYER_HEAD);
        when(item.getItemMeta()).thenReturn(meta);
        when(meta.getPersistentDataContainer()).thenReturn(pdc);
        when(pdc.has(eq(manager.getRecipeKey()), eq(PersistentDataType.BYTE))).thenReturn(true);
        when(dbManager.getDeadPlayers()).thenReturn(List.of());

        manager.onPlayerInteract(event);

        verify(event).setCancelled(true);

        ArgumentCaptor<Runnable> asyncCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).runTaskAsynchronously(eq(plugin), asyncCaptor.capture());

        // Execute async task
        asyncCaptor.getValue().run();

        ArgumentCaptor<Runnable> syncCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).runTask(eq(plugin), syncCaptor.capture());

        // Execute sync task
        syncCaptor.getValue().run();

        verify(player).sendMessage(MessageUtil.colorize("\u00a77No dead players found."));
    }

    @Test
    void testOnPlayerInteract_validSkullWithDeadPlayers() {
        PlayerInteractEvent event = mock(PlayerInteractEvent.class);
        Player player = mock(Player.class);
        ItemStack item = mock(ItemStack.class);
        ItemMeta meta = mock(ItemMeta.class);
        PersistentDataContainer pdc = mock(PersistentDataContainer.class);

        when(event.getAction()).thenReturn(Action.RIGHT_CLICK_BLOCK);
        when(event.getItem()).thenReturn(item);
        when(event.getPlayer()).thenReturn(player);
        when(item.getType()).thenReturn(Material.PLAYER_HEAD);
        when(item.getItemMeta()).thenReturn(meta);
        when(meta.getPersistentDataContainer()).thenReturn(pdc);
        when(pdc.has(eq(manager.getRecipeKey()), eq(PersistentDataType.BYTE))).thenReturn(true);

        UUID deadUuid = UUID.randomUUID();
        PlayerData deadData = new PlayerData(deadUuid, "DeadPlayer", true, 0, System.currentTimeMillis());
        when(dbManager.getDeadPlayers()).thenReturn(List.of(deadData));

        Inventory inventory = mock(Inventory.class);
        SkullMeta headMeta = mock(SkullMeta.class);
        OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);

        mockedBukkit.when(() -> Bukkit.createInventory(eq(null), anyInt(), any(Component.class))).thenReturn(inventory);
        mockedBukkit.when(() -> Bukkit.getOfflinePlayer(deadUuid)).thenReturn(offlinePlayer);
        when(itemFactory.getItemMeta(Material.PLAYER_HEAD)).thenReturn(headMeta);

        manager.onPlayerInteract(event);

        verify(event).setCancelled(true);

        ArgumentCaptor<Runnable> asyncCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).runTaskAsynchronously(eq(plugin), asyncCaptor.capture());

        // Execute async task
        asyncCaptor.getValue().run();

        ArgumentCaptor<Runnable> syncCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).runTask(eq(plugin), syncCaptor.capture());

        // Execute sync task (openMenu)
        syncCaptor.getValue().run();

        verify(player).openInventory(inventory);
        verify(inventory).setItem(eq(0), any(ItemStack.class));
    }

    @Test
    void testOnInventoryClick_otherTitle() {
        InventoryClickEvent event = mock(InventoryClickEvent.class);
        InventoryView view = mock(InventoryView.class);
        when(event.getView()).thenReturn(view);
        when(view.title()).thenReturn(Component.text("Different Inventory"));

        manager.onInventoryClick(event);

        verify(event, never()).setCancelled(true);
    }

    @Test
    void testOnInventoryClick_clickedInventoryNull() {
        InventoryClickEvent event = mock(InventoryClickEvent.class);
        InventoryView view = mock(InventoryView.class);
        when(event.getView()).thenReturn(view);
        when(view.title()).thenReturn(Component.text("Revive - Select Player", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
        when(event.getClickedInventory()).thenReturn(null);

        manager.onInventoryClick(event);

        verify(event).setCancelled(true);
        verify(event, never()).getCurrentItem();
    }

    @Test
    void testOnInventoryClick_clickedBottomInventory() {
        InventoryClickEvent event = mock(InventoryClickEvent.class);
        InventoryView view = mock(InventoryView.class);
        Inventory topInv = mock(Inventory.class);
        Inventory bottomInv = mock(Inventory.class);

        when(event.getView()).thenReturn(view);
        when(view.title()).thenReturn(Component.text("Revive - Select Player", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
        when(view.getTopInventory()).thenReturn(topInv);
        when(event.getClickedInventory()).thenReturn(bottomInv);

        manager.onInventoryClick(event);

        verify(event).setCancelled(true);
        verify(event, never()).getCurrentItem();
    }

    @Test
    void testOnInventoryClick_nonHeadItem() {
        InventoryClickEvent event = mock(InventoryClickEvent.class);
        InventoryView view = mock(InventoryView.class);
        Inventory topInv = mock(Inventory.class);
        ItemStack clicked = mock(ItemStack.class);

        when(event.getView()).thenReturn(view);
        when(view.title()).thenReturn(Component.text("Revive - Select Player", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
        when(view.getTopInventory()).thenReturn(topInv);
        when(event.getClickedInventory()).thenReturn(topInv);
        when(event.getCurrentItem()).thenReturn(clicked);
        when(clicked.getType()).thenReturn(Material.DIRT);

        manager.onInventoryClick(event);

        verify(event).setCancelled(true);
    }

    @Test
    void testOnInventoryClick_validHeadClick() {
        InventoryClickEvent event = mock(InventoryClickEvent.class);
        InventoryView view = mock(InventoryView.class);
        Inventory topInv = mock(Inventory.class);
        ItemStack clicked = mock(ItemStack.class);
        SkullMeta skullMeta = mock(SkullMeta.class);
        OfflinePlayer owner = mock(OfflinePlayer.class);
        Player player = mock(Player.class);
        PlayerInventory playerInv = mock(PlayerInventory.class);

        when(event.getView()).thenReturn(view);
        when(view.title()).thenReturn(Component.text("Revive - Select Player", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
        when(view.getTopInventory()).thenReturn(topInv);
        when(event.getClickedInventory()).thenReturn(topInv);
        when(event.getCurrentItem()).thenReturn(clicked);
        when(clicked.getType()).thenReturn(Material.PLAYER_HEAD);
        when(clicked.getItemMeta()).thenReturn(skullMeta);
        when(skullMeta.getOwningPlayer()).thenReturn(owner);
        when(owner.getName()).thenReturn("DeadPlayer");
        when(event.getWhoClicked()).thenReturn(player);
        when(player.getInventory()).thenReturn(playerInv);

        SkullMeta newHeadMeta = mock(SkullMeta.class);
        when(itemFactory.getItemMeta(Material.PLAYER_HEAD)).thenReturn(newHeadMeta);

        manager.onInventoryClick(event);

        verify(event).setCancelled(true);
        verify(newHeadMeta).setOwningPlayer(owner);
        verify(playerInv).addItem(any(ItemStack.class));
        verify(player).closeInventory();
        verify(player).sendMessage(MessageUtil.colorize("\u0026aReceived \u0026eDeadPlayer\u0026a's head."));
    }

    @Test
    void testOnInventoryDrag_matchingTitle() {
        InventoryDragEvent event = mock(InventoryDragEvent.class);
        InventoryView view = mock(InventoryView.class);
        when(event.getView()).thenReturn(view);
        when(view.title()).thenReturn(Component.text("Revive - Select Player", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));

        manager.onInventoryDrag(event);

        verify(event).setCancelled(true);
    }

    @Test
    void testOnInventoryDrag_nonMatchingTitle() {
        InventoryDragEvent event = mock(InventoryDragEvent.class);
        InventoryView view = mock(InventoryView.class);
        when(event.getView()).thenReturn(view);
        when(view.title()).thenReturn(Component.text("Other Title"));

        manager.onInventoryDrag(event);

        verify(event, never()).setCancelled(true);
    }
}
