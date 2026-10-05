package org.ssoggy.ssoggysouls.hrm;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.ssoggy.ssoggysouls.SSoggySouls;
import org.ssoggy.ssoggysouls.hrm.dlc.enums.GAMEMODESENUM;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStatic;

import net.kyori.adventure.text.Component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyChar;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviveSkullManagerTest {

    private MockedStatic<Bukkit> mockedBukkit;
    private Server server;
    private ItemFactory itemFactory;
    private ItemMeta itemMeta;
    private PersistentDataContainer pdc;

    private SSoggySouls plugin;
    private ReviveSkullManager manager;

    @BeforeAll
    static void initGlobalStatic() throws Exception {
        File dir = new File("build/tmp/test_reviveskull_static");
        dir.mkdirs();
        File f = new File(dir, "ghostmodeplayers.yml");
        if (!f.exists()) {
            Files.writeString(f.toPath(), "ghostmodeplayers:\n  \"00000000-0000-0000-0000-000000000000\": \"Player1\"\n");
        }

        JavaPlugin mockPlugin = mock(JavaPlugin.class);
        when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("test"));
        when(mockPlugin.getDataFolder()).thenReturn(dir);
        RPStatic.CLIENT = mockPlugin;

        assertNotNull(GAMEMODESENUM.SURVIVAL);
    }

    @BeforeEach
    void setUp() {
        mockedBukkit = mockStatic(Bukkit.class);
        server = mock(Server.class);
        itemFactory = mock(ItemFactory.class);

        mockedBukkit.when(Bukkit::getServer).thenReturn(server);
        mockedBukkit.when(Bukkit::getItemFactory).thenReturn(itemFactory);
        when(server.getItemFactory()).thenReturn(itemFactory);

        itemMeta = mock(ItemMeta.class);
        pdc = mock(PersistentDataContainer.class);
        when(itemFactory.getItemMeta(any(Material.class))).thenReturn(itemMeta);
        when(itemMeta.getPersistentDataContainer()).thenReturn(pdc);

        plugin = mock(SSoggySouls.class);
        when(plugin.getName()).thenReturn("SSoggySouls");
        manager = new ReviveSkullManager(plugin);
    }

    @AfterEach
    void tearDown() {
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
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
        when(item.getType()).thenReturn(Material.PLAYER_HEAD);
        when(item.getItemMeta()).thenReturn(itemMeta);
        when(pdc.has(any(NamespacedKey.class), eq(PersistentDataType.BYTE))).thenReturn(false);

        assertFalse(manager.isReviveSkull(item));
    }

    @Test
    void testIsReviveSkull_withValidTag() {
        ItemStack item = mock(ItemStack.class);
        when(item.getType()).thenReturn(Material.PLAYER_HEAD);
        when(item.getItemMeta()).thenReturn(itemMeta);
        when(pdc.has(any(NamespacedKey.class), eq(PersistentDataType.BYTE))).thenReturn(true);

        assertTrue(manager.isReviveSkull(item));
    }

    @Test
    void testCreateReviveSkullItem_success() {
        try (MockedConstruction<ItemStack> mockedItem = mockConstruction(ItemStack.class, (mock, context) -> {
            assertEquals(Material.PLAYER_HEAD, context.arguments().get(0));
            when(mock.getItemMeta()).thenReturn(itemMeta);
        })) {
            ItemStack result = manager.createReviveSkullItem();

            assertNotNull(result);
            verify(itemMeta).displayName(any(Component.class));
            verify(itemMeta).lore(any(List.class));
            verify(pdc).set(eq(manager.getRecipeKey()), eq(PersistentDataType.BYTE), eq((byte) 1));
            verify(result).setItemMeta(itemMeta);
        }
    }

    @Test
    void testCreateReviveSkullItem_nullMeta() {
        try (MockedConstruction<ItemStack> mockedItem = mockConstruction(ItemStack.class, (mock, context) -> {
            assertEquals(Material.PLAYER_HEAD, context.arguments().get(0));
            when(mock.getItemMeta()).thenReturn(null);
        })) {
            ItemStack result = manager.createReviveSkullItem();

            assertNotNull(result);
            verify(result, never()).setItemMeta(any());
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
    void testRegisterRecipe() {
        try (MockedConstruction<ItemStack> mockedItem = mockConstruction(ItemStack.class, (mock, context) -> {
                 when(mock.getItemMeta()).thenReturn(itemMeta);
             });
             MockedConstruction<RecipeChoice.MaterialChoice> mockedChoice = mockConstruction(RecipeChoice.MaterialChoice.class);
             MockedConstruction<ShapedRecipe> mockedRecipe = mockConstruction(ShapedRecipe.class, (mock, context) -> {
                 when(mock.shape(any(String[].class))).thenReturn(mock);
                 when(mock.setIngredient(anyChar(), any(Material.class))).thenReturn(mock);
                 when(mock.setIngredient(anyChar(), any(RecipeChoice.class))).thenReturn(mock);
             })) {

            manager.registerRecipe();

            assertEquals(1, mockedRecipe.constructed().size());
            ShapedRecipe recipeMock = mockedRecipe.constructed().get(0);

            verify(recipeMock).shape("OGO", "TST", "OGO");
            verify(recipeMock).setIngredient('O', Material.OBSIDIAN);
            verify(recipeMock).setIngredient('G', Material.GHAST_TEAR);
            verify(recipeMock).setIngredient('T', Material.TOTEM_OF_UNDYING);
            verify(recipeMock).setIngredient(eq('S'), any(RecipeChoice.MaterialChoice.class));

            mockedBukkit.verify(() -> Bukkit.removeRecipe(any(NamespacedKey.class)));
            mockedBukkit.verify(() -> Bukkit.addRecipe(recipeMock));
            verify(plugin).debug("Registered Revive Skull crafting recipe.");
        }
    }

    @Test
    void testUnregisterRecipe() {
        manager.unregisterRecipe();

        ArgumentCaptor<NamespacedKey> captor = ArgumentCaptor.forClass(NamespacedKey.class);
        mockedBukkit.verify(() -> Bukkit.removeRecipe(captor.capture()));

        NamespacedKey key = captor.getValue();
        assertNotNull(key);
        assertEquals("ssoggysouls", key.getNamespace());
        assertEquals("revive_skull", key.getKey());
    }
}
