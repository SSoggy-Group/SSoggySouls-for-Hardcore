package org.ssoggy.ssoggysouls.hrm;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ReviveSkullManagerTest {

    @Test
    void testIsReviveSkull_NullAndEmpty() {
        assertFalse(ReviveSkullManager.isReviveSkull(null));
        assertFalse(ReviveSkullManager.isReviveSkull(ItemStack.EMPTY));
    }

    @Test
    void testHandleSlotClick_OutOfBoundsAndNullSlots() {
        // Null slots list
        assertDoesNotThrow(() -> ReviveSkullManager.handleSlotClick(null, 0, 9, null, null));

        // Index negative
        List<Slot> slots = new ArrayList<>();
        assertDoesNotThrow(() -> ReviveSkullManager.handleSlotClick(slots, -1, 9, null, null));

        // Index >= numSlots
        assertDoesNotThrow(() -> ReviveSkullManager.handleSlotClick(slots, 10, 9, null, null));

        // Index >= slots.size()
        assertDoesNotThrow(() -> ReviveSkullManager.handleSlotClick(slots, 0, 9, null, null));
    }

    @Test
    void testHandleSlotClick_ValidSlotWithEmptyItem() {
        SimpleContainer container = new SimpleContainer(1);
        Slot slot = new Slot(container, 0, 0, 0);
        List<Slot> slots = List.of(slot);

        assertDoesNotThrow(() -> ReviveSkullManager.handleSlotClick(slots, 0, 9, null, null));
    }

    @Test
    void testHandleMenuClick_NullOrNonPlayerPlayer() {
        assertDoesNotThrow(() -> ReviveSkullManager.handleMenuClick(null, null, null));
        assertDoesNotThrow(() -> ReviveSkullManager.handleMenuClick(ItemStack.EMPTY, null, null));
    }
}
