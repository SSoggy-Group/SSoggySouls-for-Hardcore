package org.ssoggy.ssoggysouls.hrm.dlc.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class GhostStateTest {

    @Test
    void testLoadEmptyTag() {
        CompoundTag tag = new CompoundTag();
        GhostState state = GhostState.load(tag);
        assertNotNull(state);
    }

    @Test
    void testLoadCorruptKeysSkippedGracefully() {
        CompoundTag tag = new CompoundTag();

        CompoundTag locations = new CompoundTag();
        UUID validUuid = UUID.randomUUID();
        locations.putLong(validUuid.toString(), BlockPos.asLong(10, 64, 20));
        locations.putLong("corrupt-uuid-key", 12345L);
        tag.put("deathLocations", locations);

        CompoundTag dimensions = new CompoundTag();
        dimensions.putString(validUuid.toString(), "minecraft:overworld");
        dimensions.putString("corrupt-uuid-key", "minecraft:the_nether");
        tag.put("deathDimensions", dimensions);

        CompoundTag holders = new CompoundTag();
        UUID holderUuid = UUID.randomUUID();
        holders.putString(validUuid.toString(), holderUuid.toString());
        holders.putString("corrupt-uuid-key", holderUuid.toString());
        holders.putString(UUID.randomUUID().toString(), "invalid-holder-uuid");
        tag.put("deathHolders", holders);

        CompoundTag headLocations = new CompoundTag();
        headLocations.put("corrupt-uuid-key", new net.minecraft.nbt.ListTag());
        tag.put("headBlockLocations", headLocations);

        GhostState state = assertDoesNotThrow(() -> GhostState.load(tag));

        assertNotNull(state);
        assertEquals(BlockPos.of(BlockPos.asLong(10, 64, 20)), state.getDeathLocation(validUuid));
        assertEquals(holderUuid, state.getDeathHolder(validUuid));
        assertNull(state.getDeathLocation(UUID.nameUUIDFromBytes("corrupt-uuid-key".getBytes())));
    }
}
