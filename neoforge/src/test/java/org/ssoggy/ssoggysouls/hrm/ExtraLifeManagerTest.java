package org.ssoggy.ssoggysouls.hrm;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.util.ConfigManager;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExtraLifeManagerTest {

    private DatabaseManager db;
    private PlayerInteractEvent.RightClickItem event;
    private ServerPlayer serverPlayer;
    private Level level;

    @BeforeAll
    static void initMinecraft() {
        try {
            SharedConstants.tryDetectVersion();
            Bootstrap.bootStrap();
        } catch (Throwable ignored) {
            // Handled if already bootstrapped
        }
    }

    @BeforeEach
    void setUp() {
        db = mock(DatabaseManager.class);
        event = mock(PlayerInteractEvent.RightClickItem.class);
        serverPlayer = mock(ServerPlayer.class);
        level = mock(Level.class);

        ConfigManager.getConfig().setHrmEnabled(true);
        ExtraLifeManager.register(db);

        when(event.getLevel()).thenReturn(level);
        when(level.isClientSide()).thenReturn(false);
        when(event.getEntity()).thenReturn(serverPlayer);
        when(serverPlayer.getUUID()).thenReturn(UUID.randomUUID());
        when(serverPlayer.getScoreboardName()).thenReturn("TestPlayer");
    }

    @Test
    void testRegister() {
        ExtraLifeManager.register(db);
        when(event.getItemStack()).thenReturn(ItemStack.EMPTY);
        ExtraLifeManager.onItemRightClick(event);
        verify(event, never()).setCanceled(true);
    }

    @Test
    void testOnItemRightClick_hrmDisabled() {
        ConfigManager.getConfig().setHrmEnabled(false);
        when(event.getItemStack()).thenReturn(ExtraLifeManager.createExtraLifeItem());

        ExtraLifeManager.onItemRightClick(event);

        verify(event, never()).setCanceled(true);
    }

    @Test
    void testOnItemRightClick_nullDb() {
        ExtraLifeManager.register(null);
        when(event.getItemStack()).thenReturn(ExtraLifeManager.createExtraLifeItem());

        ExtraLifeManager.onItemRightClick(event);

        verify(event, never()).setCanceled(true);
    }

    @Test
    void testOnItemRightClick_clientSide() {
        when(level.isClientSide()).thenReturn(true);
        when(event.getItemStack()).thenReturn(ExtraLifeManager.createExtraLifeItem());

        ExtraLifeManager.onItemRightClick(event);

        verify(event, never()).setCanceled(true);
    }

    @Test
    void testOnItemRightClick_notServerPlayer() {
        when(event.getEntity()).thenReturn(null);
        when(event.getItemStack()).thenReturn(ExtraLifeManager.createExtraLifeItem());

        ExtraLifeManager.onItemRightClick(event);

        verify(event, never()).setCanceled(true);
    }

    @Test
    void testOnItemRightClick_notExtraLifeItem() {
        ItemStack regularItem = new ItemStack(Items.STONE, 1);
        when(event.getItemStack()).thenReturn(regularItem);

        ExtraLifeManager.onItemRightClick(event);

        assertEquals(1, regularItem.getCount());
        verify(event, never()).setCanceled(true);
    }

    @Test
    void testOnItemRightClick_validItem_creative() {
        ItemStack extraLifeItem = ExtraLifeManager.createExtraLifeItem();
        when(event.getItemStack()).thenReturn(extraLifeItem);
        when(serverPlayer.isCreative()).thenReturn(true);

        ExtraLifeManager.onItemRightClick(event);

        assertEquals(1, extraLifeItem.getCount());
        verify(event).setCanceled(true);
    }

    @Test
    void testOnItemRightClick_validItem_survival() {
        ItemStack extraLifeItem = ExtraLifeManager.createExtraLifeItem();
        when(event.getItemStack()).thenReturn(extraLifeItem);
        when(serverPlayer.isCreative()).thenReturn(false);

        ExtraLifeManager.onItemRightClick(event);

        assertEquals(0, extraLifeItem.getCount());
        verify(event).setCanceled(true);
    }

    @Test
    void testIsExtraLifeItem_emptyStack() {
        assertFalse(ExtraLifeManager.isExtraLifeItem(ItemStack.EMPTY));
    }

    @Test
    void testIsExtraLifeItem_regularItem() {
        ItemStack stone = new ItemStack(Items.STONE);
        assertFalse(ExtraLifeManager.isExtraLifeItem(stone));
    }

    @Test
    void testIsExtraLifeItem_validCustomData() {
        ItemStack extraLifeItem = ExtraLifeManager.createExtraLifeItem();
        assertTrue(ExtraLifeManager.isExtraLifeItem(extraLifeItem));
    }

    @Test
    void testCreateExtraLifeItem() {
        ItemStack created = ExtraLifeManager.createExtraLifeItem();
        assertNotNull(created);
        assertTrue(ExtraLifeManager.isExtraLifeItem(created));
    }
}
