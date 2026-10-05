package org.ssoggy.ssoggysouls.hrm;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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
import static org.mockito.ArgumentMatchers.any;
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
        assertFalse(ExtraLifeManager.onItemRightClick(event));
    }

    @Test
    void testOnItemRightClick_hrmDisabled_returnsFalse() {
        ConfigManager.getConfig().setHrmEnabled(false);
        when(event.getItemStack()).thenReturn(ExtraLifeManager.createExtraLifeItem());

        boolean result = ExtraLifeManager.onItemRightClick(event);

        assertFalse(result);
        verify(event, never()).setCancellationResult(any());
    }

    @Test
    void testOnItemRightClick_nullDb_returnsFalse() {
        ExtraLifeManager.register(null);
        when(event.getItemStack()).thenReturn(ExtraLifeManager.createExtraLifeItem());

        boolean result = ExtraLifeManager.onItemRightClick(event);

        assertFalse(result);
        verify(event, never()).setCancellationResult(any());
    }

    @Test
    void testOnItemRightClick_clientSide_returnsFalse() {
        when(level.isClientSide()).thenReturn(true);
        when(event.getItemStack()).thenReturn(ExtraLifeManager.createExtraLifeItem());

        boolean result = ExtraLifeManager.onItemRightClick(event);

        assertFalse(result);
        verify(event, never()).setCancellationResult(any());
    }

    @Test
    void testOnItemRightClick_notServerPlayer_returnsFalse() {
        when(event.getEntity()).thenReturn(null);
        when(event.getItemStack()).thenReturn(ExtraLifeManager.createExtraLifeItem());

        boolean result = ExtraLifeManager.onItemRightClick(event);

        assertFalse(result);
        verify(event, never()).setCancellationResult(any());
    }

    @Test
    void testOnItemRightClick_notExtraLifeItem_returnsFalse() {
        ItemStack regularItem = new ItemStack(Items.STONE, 1);
        when(event.getItemStack()).thenReturn(regularItem);

        boolean result = ExtraLifeManager.onItemRightClick(event);

        assertFalse(result);
        assertEquals(1, regularItem.getCount());
        verify(event, never()).setCancellationResult(any());
    }

    @Test
    void testOnItemRightClick_validItem_creative_returnsTrueAndNoShrink() {
        ItemStack extraLifeItem = ExtraLifeManager.createExtraLifeItem();
        when(event.getItemStack()).thenReturn(extraLifeItem);
        when(serverPlayer.isCreative()).thenReturn(true);

        boolean result = ExtraLifeManager.onItemRightClick(event);

        assertTrue(result);
        assertEquals(1, extraLifeItem.getCount());
        verify(event).setCancellationResult(InteractionResult.CONSUME);
    }

    @Test
    void testOnItemRightClick_validItem_survival_returnsTrueAndShrinks() {
        ItemStack extraLifeItem = ExtraLifeManager.createExtraLifeItem();
        when(event.getItemStack()).thenReturn(extraLifeItem);
        when(serverPlayer.isCreative()).thenReturn(false);

        boolean result = ExtraLifeManager.onItemRightClick(event);

        assertTrue(result);
        assertEquals(0, extraLifeItem.getCount());
        verify(event).setCancellationResult(InteractionResult.CONSUME);
    }

    @Test
    void testIsExtraLifeItem_emptyStack_returnsFalse() {
        assertFalse(ExtraLifeManager.isExtraLifeItem(ItemStack.EMPTY));
    }

    @Test
    void testIsExtraLifeItem_regularItem_returnsFalse() {
        ItemStack stone = new ItemStack(Items.STONE);
        assertFalse(ExtraLifeManager.isExtraLifeItem(stone));
    }

    @Test
    void testIsExtraLifeItem_validItem_returnsTrue() {
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
