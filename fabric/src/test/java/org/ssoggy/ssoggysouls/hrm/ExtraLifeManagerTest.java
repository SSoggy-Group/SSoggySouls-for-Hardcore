package org.ssoggy.ssoggysouls.hrm;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.BeforeAll;
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
        serverPlayer = mock(ServerPlayer.class);
        level = mock(Level.class);

        when(level.isClientSide()).thenReturn(false);
        when(serverPlayer.getUUID()).thenReturn(UUID.randomUUID());
        when(serverPlayer.getScoreboardName()).thenReturn("TestPlayer");
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
