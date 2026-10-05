package org.ssoggy.ssoggysouls.hrm;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ssoggy.ssoggysouls.database.DatabaseManager;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExtraLifeManagerTest {

    private DatabaseManager db;
    private ServerPlayer serverPlayer;
    private Level level;
    private ItemStack itemStack;

    @BeforeEach
    void setUp() {
        db = mock(DatabaseManager.class);
        serverPlayer = mock(ServerPlayer.class);
        level = mock(Level.class);
        itemStack = mock(ItemStack.class);

        when(level.isClientSide()).thenReturn(false);
        when(serverPlayer.getItemInHand(InteractionHand.MAIN_HAND)).thenReturn(itemStack);
        when(serverPlayer.getUUID()).thenReturn(UUID.randomUUID());
        when(serverPlayer.getScoreboardName()).thenReturn("TestPlayer");
    }

    @Test
    void testIsExtraLifeItem_emptyStack() {
        ItemStack emptyStack = mock(ItemStack.class);
        when(emptyStack.isEmpty()).thenReturn(true);

        assertFalse(ExtraLifeManager.isExtraLifeItem(emptyStack));
    }

    @Test
    void testIsExtraLifeItem_noCustomData() {
        ItemStack stackWithoutData = mock(ItemStack.class);
        when(stackWithoutData.isEmpty()).thenReturn(false);
        when(stackWithoutData.has(DataComponents.CUSTOM_DATA)).thenReturn(false);

        assertFalse(ExtraLifeManager.isExtraLifeItem(stackWithoutData));
    }

    @Test
    void testIsExtraLifeItem_validCustomData() {
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
