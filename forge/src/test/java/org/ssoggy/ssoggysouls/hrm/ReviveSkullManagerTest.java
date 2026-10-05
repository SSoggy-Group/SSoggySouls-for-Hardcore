package org.ssoggy.ssoggysouls.hrm;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import org.ssoggy.ssoggysouls.database.DatabaseManager;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReviveSkullManagerTest {

    @Test
    void testIsReviveSkull_NullAndEmpty() {
        assertFalse(ReviveSkullManager.isReviveSkull(null));

        ItemStack emptyStack = mock(ItemStack.class);
        when(emptyStack.isEmpty()).thenReturn(true);
        assertFalse(ReviveSkullManager.isReviveSkull(emptyStack));
    }

    @Test
    void testHandleSlotClick_OutOfBoundsAndNullSlots() {
        Player player = mock(Player.class);
        DatabaseManager db = mock(DatabaseManager.class);

        // Null slots list
        assertDoesNotThrow(() -> ReviveSkullManager.handleSlotClick(null, 0, 9, player));

        // Index negative
        List<Slot> slots = new ArrayList<>();
        assertDoesNotThrow(() -> ReviveSkullManager.handleSlotClick(slots, -1, 9, player));

        // Index >= numSlots
        assertDoesNotThrow(() -> ReviveSkullManager.handleSlotClick(slots, 10, 9, player));

        // Index >= slots.size()
        assertDoesNotThrow(() -> ReviveSkullManager.handleSlotClick(slots, 0, 9, player));
    }

    @Test
    void testHandleSlotClick_ValidSlotWithEmptyOrNonHeadItem() {
        Player player = mock(Player.class);
        DatabaseManager db = mock(DatabaseManager.class);
        Slot slot = mock(Slot.class);
        ItemStack stack = mock(ItemStack.class);

        when(slot.getItem()).thenReturn(stack);
        when(stack.isEmpty()).thenReturn(false);
        when(stack.is(Items.PLAYER_HEAD)).thenReturn(false);

        List<Slot> slots = List.of(slot);

        assertDoesNotThrow(() -> ReviveSkullManager.handleSlotClick(slots, 0, 9, player));
    }

    @Test
    void testHandleMenuClick_NullOrNonPlayerPlayer() {
        DatabaseManager db = mock(DatabaseManager.class);
        ItemStack head = mock(ItemStack.class);
        Player player = mock(Player.class); // Not ServerPlayer

        when(head.isEmpty()).thenReturn(false);
        when(head.is(Items.PLAYER_HEAD)).thenReturn(true);

        assertDoesNotThrow(() -> ReviveSkullManager.handleMenuClick(null, player));
        assertDoesNotThrow(() -> ReviveSkullManager.handleMenuClick(head, player));
        assertDoesNotThrow(() -> ReviveSkullManager.handleMenuClick(head, null));
    }
}
