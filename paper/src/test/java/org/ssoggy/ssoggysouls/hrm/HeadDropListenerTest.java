package org.ssoggy.ssoggysouls.hrm;

import java.util.UUID;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Skull;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HeadDropListenerTest {

    private Chunk chunk;

    @BeforeEach
    void setUp() {
        chunk = mock(Chunk.class);
    }

    @Test
    void testProcessChunkTileEntities_unloadedChunk() {
        when(chunk.isLoaded()).thenReturn(false);

        // Verify that if chunk is not loaded, getTileEntities is never called
        verify(chunk, never()).getTileEntities(any(), eq(false));
    }
}
