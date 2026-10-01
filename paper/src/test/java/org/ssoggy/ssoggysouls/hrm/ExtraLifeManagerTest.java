package org.ssoggy.ssoggysouls.hrm;

import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
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
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.hrm.dlc.enums.GAMEMODESENUM;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStatic;

import java.io.File;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ExtraLifeManagerTest {

    private MockedStatic<Bukkit> mockedBukkit;
    private Server server;
    private ItemFactory itemFactory;

    private SSoggySouls plugin;
    private FileConfiguration config;
    private Logger logger;
    private ItemMeta itemMeta;
    private PersistentDataContainer pdc;
    private ExtraLifeManager manager;

    @BeforeAll
    static void initGlobalStatic() throws Exception {
        File dir = new File("build/tmp/test_extralife_static");
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
        PluginMeta pluginMeta = mock(PluginMeta.class);
        when(plugin.getPluginMeta()).thenReturn(pluginMeta);
        when(pluginMeta.getName()).thenReturn("ssoggysouls");

        config = mock(FileConfiguration.class);
        logger = mock(Logger.class);
        DatabaseManager db = mock(DatabaseManager.class);

        when(plugin.getName()).thenReturn("SSoggySouls");
        when(plugin.getConfig()).thenReturn(config);
        when(plugin.getLogger()).thenReturn(logger);
        when(plugin.getDatabaseManager()).thenReturn(db);

        manager = new ExtraLifeManager(plugin);
    }

    @AfterEach
    void tearDown() {
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
    }

    @Test
    void testRegisterRecipe_defaultIngredients() {
        when(config.getString("extra-life.recipe.row1", "GEG")).thenReturn("GEG");
        when(config.getString("extra-life.recipe.row2", "ENE")).thenReturn("ENE");
        when(config.getString("extra-life.recipe.row3", "GEG")).thenReturn("GEG");
        when(config.getConfigurationSection("extra-life.recipe.ingredients")).thenReturn(null);

        try (MockedConstruction<ItemStack> mockedItem = mockConstruction(ItemStack.class);
             MockedConstruction<ShapedRecipe> mockedRecipe = mockConstruction(ShapedRecipe.class, (mock, context) -> {
                 when(mock.shape(any(String[].class))).thenReturn(mock);
                 when(mock.setIngredient(anyChar(), any(Material.class))).thenReturn(mock);
             })) {
            manager.registerRecipe();

            assertEquals(1, mockedRecipe.constructed().size());
            ShapedRecipe recipeMock = mockedRecipe.constructed().get(0);

            verify(recipeMock).shape("GEG", "ENE", "GEG");
            verify(recipeMock).setIngredient('G', Material.GOLD_BLOCK);
            verify(recipeMock).setIngredient('E', Material.EMERALD);
            verify(recipeMock).setIngredient('N', Material.NETHER_STAR);

            mockedBukkit.verify(() -> Bukkit.addRecipe(recipeMock));
            verify(plugin).debug("Registered Extra Life crafting recipe.");
        }
    }

    @Test
    void testRegisterRecipe_customIngredients() {
        when(config.getString("extra-life.recipe.row1", "GEG")).thenReturn("ABA");
        when(config.getString("extra-life.recipe.row2", "ENE")).thenReturn("BAB");
        when(config.getString("extra-life.recipe.row3", "GEG")).thenReturn("ABA");

        ConfigurationSection section = mock(ConfigurationSection.class);
        when(config.getConfigurationSection("extra-life.recipe.ingredients")).thenReturn(section);
        Set<String> keys = new LinkedHashSet<>();
        keys.add("A");
        keys.add("B");
        when(section.getKeys(false)).thenReturn(keys);
        when(section.getString("A", "STONE")).thenReturn("DIAMOND");
        when(section.getString("B", "STONE")).thenReturn("EMERALD");

        try (MockedConstruction<ItemStack> mockedItem = mockConstruction(ItemStack.class);
             MockedConstruction<ShapedRecipe> mockedRecipe = mockConstruction(ShapedRecipe.class, (mock, context) -> {
                 when(mock.shape(any(String[].class))).thenReturn(mock);
                 when(mock.setIngredient(anyChar(), any(Material.class))).thenReturn(mock);
             })) {
            manager.registerRecipe();

            assertEquals(1, mockedRecipe.constructed().size());
            ShapedRecipe recipeMock = mockedRecipe.constructed().get(0);

            verify(recipeMock).shape("ABA", "BAB", "ABA");
            verify(recipeMock).setIngredient('A', Material.DIAMOND);
            verify(recipeMock).setIngredient('B', Material.EMERALD);

            mockedBukkit.verify(() -> Bukkit.addRecipe(recipeMock));
        }
    }

    @Test
    void testRegisterRecipe_invalidIngredientKeyOrMaterial() {
        when(config.getString("extra-life.recipe.row1", "GEG")).thenReturn("GEG");
        when(config.getString("extra-life.recipe.row2", "ENE")).thenReturn("ENE");
        when(config.getString("extra-life.recipe.row3", "GEG")).thenReturn("GEG");

        ConfigurationSection section = mock(ConfigurationSection.class);
        when(config.getConfigurationSection("extra-life.recipe.ingredients")).thenReturn(section);
        Set<String> keys = new LinkedHashSet<>();
        keys.add("INVALID_KEY");
        keys.add("X");
        when(section.getKeys(false)).thenReturn(keys);
        when(section.getString("INVALID_KEY", "STONE")).thenReturn("DIAMOND");
        when(section.getString("X", "STONE")).thenReturn("NON_EXISTENT_MATERIAL");

        try (MockedConstruction<ItemStack> mockedItem = mockConstruction(ItemStack.class);
             MockedConstruction<ShapedRecipe> mockedRecipe = mockConstruction(ShapedRecipe.class, (mock, context) -> {
                 when(mock.shape(any(String[].class))).thenReturn(mock);
                 when(mock.setIngredient(anyChar(), any(Material.class))).thenReturn(mock);
             })) {
            manager.registerRecipe();

            assertEquals(1, mockedRecipe.constructed().size());
            ShapedRecipe recipeMock = mockedRecipe.constructed().get(0);

            verify(logger, times(2)).log(eq(Level.WARNING), anyString(), any(Object[].class));
            mockedBukkit.verify(() -> Bukkit.addRecipe(recipeMock));
        }
    }

    @Test
    void testUnregisterRecipe() {
        manager.unregisterRecipe();

        ArgumentCaptor<NamespacedKey> captor = ArgumentCaptor.forClass(NamespacedKey.class);
        mockedBukkit.verify(() -> Bukkit.removeRecipe(captor.capture()));

        NamespacedKey key = captor.getValue();
        assertNotNull(key);
        assertEquals("extra_life_recipe", key.getKey());
    }

    @Test
    void testCreateExtraLifeItem_defaultMaterial() {
        when(config.getString("extra-life.item-material", "NETHER_STAR")).thenReturn("NETHER_STAR");

        try (MockedConstruction<ItemStack> mockedItem = mockConstruction(ItemStack.class, (mock, context) -> {
            when(mock.getType()).thenReturn(Material.NETHER_STAR);
            when(mock.getItemMeta()).thenReturn(itemMeta);
        })) {
            ItemStack item = manager.createExtraLifeItem();

            assertNotNull(item);
            assertEquals(Material.NETHER_STAR, item.getType());
            verify(pdc).set(any(NamespacedKey.class), eq(PersistentDataType.BYTE), eq((byte) 1));
        }
    }

    @Test
    void testCreateExtraLifeItem_customMaterial() {
        when(config.getString("extra-life.item-material", "NETHER_STAR")).thenReturn("DIAMOND");

        try (MockedConstruction<ItemStack> mockedItem = mockConstruction(ItemStack.class, (mock, context) -> {
            when(mock.getType()).thenReturn(Material.DIAMOND);
            when(mock.getItemMeta()).thenReturn(itemMeta);
        })) {
            ItemStack item = manager.createExtraLifeItem();

            assertNotNull(item);
            assertEquals(Material.DIAMOND, item.getType());
        }
    }

    @Test
    void testCreateExtraLifeItem_invalidMaterialFallback() {
        when(config.getString("extra-life.item-material", "NETHER_STAR")).thenReturn("INVALID_MATERIAL_NAME");

        try (MockedConstruction<ItemStack> mockedItem = mockConstruction(ItemStack.class, (mock, context) -> {
            when(mock.getType()).thenReturn(Material.NETHER_STAR);
            when(mock.getItemMeta()).thenReturn(itemMeta);
        })) {
            ItemStack item = manager.createExtraLifeItem();

            assertNotNull(item);
            assertEquals(Material.NETHER_STAR, item.getType());
        }
    }
}
