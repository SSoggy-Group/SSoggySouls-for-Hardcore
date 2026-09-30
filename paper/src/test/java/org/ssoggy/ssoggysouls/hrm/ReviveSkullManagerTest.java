package org.ssoggy.ssoggysouls.hrm;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ssoggy.ssoggysouls.SSoggySouls;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReviveSkullManagerTest {

    private SSoggySouls plugin;
    private ReviveSkullManager manager;

    @BeforeEach
    void setUp() {
        plugin = mock(SSoggySouls.class);
        when(plugin.getName()).thenReturn("SSoggySouls");
        manager = new ReviveSkullManager(plugin);
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
}
