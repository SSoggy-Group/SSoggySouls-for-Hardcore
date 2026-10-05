package org.ssoggy.ssoggysouls.hrm;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.util.ConfigManager;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExtraLifeManagerTest {

    private DatabaseManager db;
    private PlayerInteractEvent.RightClickItem event;
    private ServerPlayer serverPlayer;
    private Level level;
    private ItemStack itemStack;

    @BeforeEach
    void setUp() {
        db = mock(DatabaseManager.class);
        event = mock(PlayerInteractEvent.RightClickItem.class);
        serverPlayer = mock(ServerPlayer.class);
        level = mock(Level.class);
        itemStack = mock(ItemStack.class);

        ConfigManager.getConfig().setHrmEnabled(true);
        ExtraLifeManager.register(db);

        when(event.getLevel()).thenReturn(level);
        when(level.isClientSide()).thenReturn(false);
        when(event.getEntity()).thenReturn(serverPlayer);
        when(event.getItemStack()).thenReturn(itemStack);
        when(serverPlayer.getUUID()).thenReturn(UUID.randomUUID());
        when(serverPlayer.getScoreboardName()).thenReturn("TestPlayer");
    }

    @Test
    void testRegister() {
        ExtraLifeManager.register(db);
        when(level.isClientSide()).thenReturn(false);
        when(itemStack.isEmpty()).thenReturn(true);
        assertFalse(ExtraLifeManager.onItemRightClick(event));
    }

    @Test
    void testOnItemRightClick_hrmDisabled_returnsFalse() {
        ConfigManager.getConfig().setHrmEnabled(false);

        boolean result = ExtraLifeManager.onItemRightClick(event);

        assertFalse(result);
        verify(event, never()).setCancellationResult(any());
    }

    @Test
    void testOnItemRightClick_nullDb_returnsFalse() {
        ExtraLifeManager.register(null);

        boolean result = ExtraLifeManager.onItemRightClick(event);

        assertFalse(result);
        verify(event, never()).setCancellationResult(any());
    }

    @Test
    void testOnItemRightClick_clientSide_returnsFalse() {
        when(level.isClientSide()).thenReturn(true);

        boolean result = ExtraLifeManager.onItemRightClick(event);

        assertFalse(result);
        verify(event, never()).setCancellationResult(any());
    }

    @Test
    void testOnItemRightClick_notServerPlayer_returnsFalse() {
        when(event.getEntity()).thenReturn(null);

        boolean result = ExtraLifeManager.onItemRightClick(event);

        assertFalse(result);
        verify(event, never()).setCancellationResult(any());
    }

    @Test
    void testOnItemRightClick_notExtraLifeItem_returnsFalse() {
        when(itemStack.isEmpty()).thenReturn(false);
        when(itemStack.has(DataComponents.CUSTOM_DATA)).thenReturn(false);

        boolean result = ExtraLifeManager.onItemRightClick(event);

        assertFalse(result);
        verify(itemStack, never()).shrink(anyInt());
        verify(event, never()).setCancellationResult(any());
    }

    @Test
    void testOnItemRightClick_validItem_creative_returnsTrueAndNoShrink() {
        setupMockExtraLifeItem(itemStack);
        when(serverPlayer.isCreative()).thenReturn(true);

        boolean result = ExtraLifeManager.onItemRightClick(event);

        assertTrue(result);
        verify(itemStack, never()).shrink(anyInt());
        verify(event).setCancellationResult(InteractionResult.CONSUME);
    }

    @Test
    void testOnItemRightClick_validItem_survival_returnsTrueAndShrinks() {
        setupMockExtraLifeItem(itemStack);
        when(serverPlayer.isCreative()).thenReturn(false);

        boolean result = ExtraLifeManager.onItemRightClick(event);

        assertTrue(result);
        verify(itemStack).shrink(1);
        verify(event).setCancellationResult(InteractionResult.CONSUME);
    }

    @Test
    void testIsExtraLifeItem_emptyStack_returnsFalse() {
        ItemStack emptyStack = mock(ItemStack.class);
        when(emptyStack.isEmpty()).thenReturn(true);

        assertFalse(ExtraLifeManager.isExtraLifeItem(emptyStack));
    }

    @Test
    void testIsExtraLifeItem_noCustomData_returnsFalse() {
        ItemStack stackWithoutData = mock(ItemStack.class);
        when(stackWithoutData.isEmpty()).thenReturn(false);
        when(stackWithoutData.has(DataComponents.CUSTOM_DATA)).thenReturn(false);

        assertFalse(ExtraLifeManager.isExtraLifeItem(stackWithoutData));
    }

    @Test
    void testIsExtraLifeItem_validCustomData_returnsTrue() {
        ItemStack extraLifeItem = mock(ItemStack.class);
        setupMockExtraLifeItem(extraLifeItem);

        assertTrue(ExtraLifeManager.isExtraLifeItem(extraLifeItem));
    }

    @Test
    void testCreateExtraLifeItem() {
        ItemStack created = ExtraLifeManager.createExtraLifeItem();
        assertNotNull(created);
        assertTrue(ExtraLifeManager.isExtraLifeItem(created));
    }

    private void setupMockExtraLifeItem(ItemStack stack) {
        when(stack.isEmpty()).thenReturn(false);
        when(stack.has(DataComponents.CUSTOM_DATA)).thenReturn(true);

        CustomData customData = mock(CustomData.class);
        CompoundTag compoundTag = mock(CompoundTag.class);
        when(compoundTag.contains("ExtraLife")).thenReturn(true);
        when(customData.copyTag()).thenReturn(compoundTag);
        when(stack.get(DataComponents.CUSTOM_DATA)).thenReturn(customData);
    }
}
