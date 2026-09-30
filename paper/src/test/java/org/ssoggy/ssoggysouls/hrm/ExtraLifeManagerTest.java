package org.ssoggy.ssoggysouls.hrm;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.ssoggy.ssoggysouls.SSoggySouls;
import org.ssoggy.ssoggysouls.database.DatabaseManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings("deprecation")
class ExtraLifeManagerTest {

    private static ItemMeta itemMeta;
    private static PersistentDataContainer pdc;

    private MockedStatic<Bukkit> mockedBukkit;
    private SSoggySouls plugin;
    private DatabaseManager db;
    private FileConfiguration config;
    private Logger logger;
    private Server server;
    private ItemFactory itemFactory;

    private ExtraLifeManager extraLifeManager;

    @BeforeAll
    static void initRegistryAccess() throws Exception {
        itemMeta = mock(ItemMeta.class);
        pdc = mock(PersistentDataContainer.class);
        when(itemMeta.getPersistentDataContainer()).thenReturn(pdc);

        Field field = Class.forName("io.papermc.paper.registry.RegistryAccessHolder").getDeclaredField("INSTANCE");
        Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
        Object base = unsafe.staticFieldBase(field);
        long offset = unsafe.staticFieldOffset(field);

        Class<?> registryAccessClass = Class.forName("io.papermc.paper.registry.RegistryAccess");
        Class<?> registryClass = Class.forName("org.bukkit.Registry", false, registryAccessClass.getClassLoader());
        Class<?> itemTypeClass = Class.forName("org.bukkit.inventory.ItemType", false, registryAccessClass.getClassLoader());
        Class<?> blockTypeClass = Class.forName("org.bukkit.block.BlockType", false, registryAccessClass.getClassLoader());
        Class<?> keyedClass = Class.forName("org.bukkit.Keyed", false, registryAccessClass.getClassLoader());

        InvocationHandler itemRegistryHandler = (proxy, method, args) -> {
            if ("get".equals(method.getName()) || "getOptional".equals(method.getName())) {
                NamespacedKey key = (args != null && args.length > 0 && args[0] instanceof NamespacedKey) ? (NamespacedKey) args[0] : null;
                Material mat = null;
                if (key != null) {
                    try {
                        mat = Material.valueOf(key.getKey().toUpperCase());
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                final Material finalMat = mat != null ? mat : Material.NETHER_STAR;

                Object itemTypeProxy = Proxy.newProxyInstance(
                        registryAccessClass.getClassLoader(),
                        new Class<?>[]{itemTypeClass},
                        (p, m, a) -> {
                            if ("createItemStack".equals(m.getName())) {
                                ItemStack delegate = mock(ItemStack.class);
                                when(delegate.getType()).thenReturn(finalMat);
                                when(delegate.getItemMeta()).thenReturn(itemMeta);
                                return delegate;
                            }
                            if (m.getReturnType() == boolean.class || m.getReturnType() == Boolean.class) {
                                return Boolean.FALSE;
                            }
                            return null;
                        }
                );

                if ("getOptional".equals(method.getName())) {
                    return Optional.of(itemTypeProxy);
                }
                return itemTypeProxy;
            }
            return null;
        };

        InvocationHandler blockRegistryHandler = (proxy, method, args) -> {
            if ("get".equals(method.getName()) || "getOptional".equals(method.getName())) {
                Object blockTypeProxy = Proxy.newProxyInstance(
                        registryAccessClass.getClassLoader(),
                        new Class<?>[]{blockTypeClass},
                        (p, m, a) -> {
                            if (m.getReturnType() == boolean.class || m.getReturnType() == Boolean.class) {
                                return Boolean.FALSE;
                            }
                            return null;
                        }
                );
                if ("getOptional".equals(method.getName())) {
                    return Optional.of(blockTypeProxy);
                }
                return blockTypeProxy;
            }
            return null;
        };

        InvocationHandler genericRegistryHandler = (proxy, method, args) -> {
            if ("get".equals(method.getName()) || "getOptional".equals(method.getName())) {
                Object keyedProxy = Proxy.newProxyInstance(
                        registryAccessClass.getClassLoader(),
                        new Class<?>[]{keyedClass},
                        (p, m, a) -> {
                            if (m.getReturnType() == boolean.class || m.getReturnType() == Boolean.class) {
                                return Boolean.FALSE;
                            }
                            return null;
                        }
                );
                if ("getOptional".equals(method.getName())) {
                    return Optional.of(keyedProxy);
                }
                return keyedProxy;
            }
            return null;
        };

        Object customAccess = Proxy.newProxyInstance(
                registryAccessClass.getClassLoader(),
                new Class<?>[]{registryAccessClass},
                (proxy, method, args) -> {
                    if ("getRegistry".equals(method.getName())) {
                        Object arg = (args != null && args.length > 0) ? args[0] : null;
                        String argStr = arg != null ? arg.toString().toLowerCase() : "";
                        InvocationHandler selectedHandler = genericRegistryHandler;
                        if (argStr.contains("block")) {
                            selectedHandler = blockRegistryHandler;
                        } else if (argStr.contains("item")) {
                            selectedHandler = itemRegistryHandler;
                        }
                        return Proxy.newProxyInstance(
                                registryAccessClass.getClassLoader(),
                                new Class<?>[]{registryClass},
                                selectedHandler
                        );
                    }
                    return null;
                }
        );

        unsafe.putObject(base, offset, Optional.of(customAccess));

        Class.forName("org.bukkit.Registry", true, registryAccessClass.getClassLoader());
    }

    @BeforeEach
    void setUp() {
        mockedBukkit = mockStatic(Bukkit.class);

        plugin = mock(SSoggySouls.class);
        db = mock(DatabaseManager.class);
        config = mock(FileConfiguration.class);
        logger = mock(Logger.class);
        server = mock(Server.class);
        itemFactory = mock(ItemFactory.class);

        clearInvocations(itemMeta, pdc);

        when(plugin.getName()).thenReturn("ssoggysouls");
        when(plugin.getDatabaseManager()).thenReturn(db);
        when(plugin.getConfig()).thenReturn(config);
        when(plugin.getLogger()).thenReturn(logger);

        when(server.getItemFactory()).thenReturn(itemFactory);
        mockedBukkit.when(Bukkit::getServer).thenReturn(server);
        mockedBukkit.when(Bukkit::getItemFactory).thenReturn(itemFactory);

        when(itemFactory.getItemMeta(any())).thenReturn(itemMeta);

        extraLifeManager = new ExtraLifeManager(plugin);
    }

    @AfterEach
    void tearDown() {
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
    }

    @Test
    void testRegisterRecipeDefaultIngredients() {
        when(config.getString("extra-life.recipe.row1", "GEG")).thenReturn("GEG");
        when(config.getString("extra-life.recipe.row2", "ENE")).thenReturn("ENE");
        when(config.getString("extra-life.recipe.row3", "GEG")).thenReturn("GEG");
        when(config.getConfigurationSection("extra-life.recipe.ingredients")).thenReturn(null);

        extraLifeManager.registerRecipe();

        ArgumentCaptor<ShapedRecipe> captor = ArgumentCaptor.forClass(ShapedRecipe.class);
        mockedBukkit.verify(() -> Bukkit.addRecipe(captor.capture()));

        ShapedRecipe recipe = captor.getValue();
        assertNotNull(recipe);
        assertEquals(new NamespacedKey(plugin, "extra_life_recipe"), recipe.getKey());
        assertArrayEquals(new String[]{"GEG", "ENE", "GEG"}, recipe.getShape());

        RecipeChoice choiceG = recipe.getChoiceMap().get('G');
        RecipeChoice choiceE = recipe.getChoiceMap().get('E');
        RecipeChoice choiceN = recipe.getChoiceMap().get('N');

        assertTrue(choiceG instanceof RecipeChoice.MaterialChoice);
        assertTrue(choiceE instanceof RecipeChoice.MaterialChoice);
        assertTrue(choiceN instanceof RecipeChoice.MaterialChoice);

        assertEquals(Material.GOLD_BLOCK, ((RecipeChoice.MaterialChoice) choiceG).getChoices().get(0));
        assertEquals(Material.EMERALD, ((RecipeChoice.MaterialChoice) choiceE).getChoices().get(0));
        assertEquals(Material.NETHER_STAR, ((RecipeChoice.MaterialChoice) choiceN).getChoices().get(0));

        verify(plugin).debug("Registered Extra Life crafting recipe.");
    }

    @Test
    void testRegisterRecipeCustomIngredients() {
        when(config.getString("extra-life.recipe.row1", "GEG")).thenReturn("D I");
        when(config.getString("extra-life.recipe.row2", "ENE")).thenReturn(" I ");
        when(config.getString("extra-life.recipe.row3", "GEG")).thenReturn("D I");

        ConfigurationSection section = mock(ConfigurationSection.class);
        when(config.getConfigurationSection("extra-life.recipe.ingredients")).thenReturn(section);
        when(section.getKeys(false)).thenReturn(Set.of("D", "I"));
        when(section.getString("D", "STONE")).thenReturn("DIAMOND");
        when(section.getString("I", "STONE")).thenReturn("IRON_INGOT");

        extraLifeManager.registerRecipe();

        ArgumentCaptor<ShapedRecipe> captor = ArgumentCaptor.forClass(ShapedRecipe.class);
        mockedBukkit.verify(() -> Bukkit.addRecipe(captor.capture()));

        ShapedRecipe recipe = captor.getValue();
        assertNotNull(recipe);
        assertArrayEquals(new String[]{"D I", " I ", "D I"}, recipe.getShape());

        RecipeChoice choiceD = recipe.getChoiceMap().get('D');
        RecipeChoice choiceI = recipe.getChoiceMap().get('I');

        assertTrue(choiceD instanceof RecipeChoice.MaterialChoice);
        assertTrue(choiceI instanceof RecipeChoice.MaterialChoice);

        assertEquals(Material.DIAMOND, ((RecipeChoice.MaterialChoice) choiceD).getChoices().get(0));
        assertEquals(Material.IRON_INGOT, ((RecipeChoice.MaterialChoice) choiceI).getChoices().get(0));
    }

    @Test
    void testRegisterRecipeInvalidIngredients() {
        when(config.getString("extra-life.recipe.row1", "GEG")).thenReturn("GEG");
        when(config.getString("extra-life.recipe.row2", "ENE")).thenReturn("ENE");
        when(config.getString("extra-life.recipe.row3", "GEG")).thenReturn("GEG");

        ConfigurationSection section = mock(ConfigurationSection.class);
        when(config.getConfigurationSection("extra-life.recipe.ingredients")).thenReturn(section);

        when(section.getKeys(false)).thenReturn(Set.of("INVALID", "X"));
        when(section.getString("INVALID", "STONE")).thenReturn("DIAMOND");
        when(section.getString("X", "STONE")).thenReturn("FAKE_MATERIAL");

        extraLifeManager.registerRecipe();

        ArgumentCaptor<ShapedRecipe> captor = ArgumentCaptor.forClass(ShapedRecipe.class);
        mockedBukkit.verify(() -> Bukkit.addRecipe(captor.capture()));

        verify(logger, times(2)).log(eq(Level.WARNING),
                eq("Invalid extra-life recipe ingredient: {0}={1}"),
                any(Object[].class));
    }

    @Test
    void testUnregisterRecipe() {
        extraLifeManager.unregisterRecipe();

        NamespacedKey expectedKey = new NamespacedKey(plugin, "extra_life_recipe");
        mockedBukkit.verify(() -> Bukkit.removeRecipe(expectedKey));
    }

    @Test
    void testCreateExtraLifeItemDefaultMaterial() {
        when(config.getString("extra-life.item-material", "NETHER_STAR")).thenReturn(null);

        ItemStack item = extraLifeManager.createExtraLifeItem();

        assertNotNull(item);
        assertEquals(Material.NETHER_STAR, item.getType());
        verify(pdc, atLeastOnce()).set(eq(new NamespacedKey(plugin, "extra_life")), eq(PersistentDataType.BYTE), eq((byte) 1));
    }

    @Test
    void testCreateExtraLifeItemCustomMaterial() {
        when(config.getString("extra-life.item-material", "NETHER_STAR")).thenReturn("DIAMOND");

        ItemStack item = extraLifeManager.createExtraLifeItem();

        assertNotNull(item);
        assertEquals(Material.DIAMOND, item.getType());
        verify(pdc, atLeastOnce()).set(eq(new NamespacedKey(plugin, "extra_life")), eq(PersistentDataType.BYTE), eq((byte) 1));
    }
}
