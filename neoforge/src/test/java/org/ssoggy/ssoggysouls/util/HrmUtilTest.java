package org.ssoggy.ssoggysouls.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.ssoggy.ssoggysouls.database.DatabaseManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class HrmUtilTest {

    private MockedStatic<ConfigManager> configManagerMock;
    private ConfigManager.ModConfig mockConfig;
    private PlayerInteractEvent event;
    private DatabaseManager db;
    private Level level;
    private ServerPlayer player;

    @BeforeEach
    void setUp() {
        configManagerMock = mockStatic(ConfigManager.class);
        mockConfig = mock(ConfigManager.ModConfig.class);
        configManagerMock.when(ConfigManager::getConfig).thenReturn(mockConfig);

        event = mock(PlayerInteractEvent.class);
        db = mock(DatabaseManager.class);
        level = mock(Level.class);
        player = mock(ServerPlayer.class);

        when(event.getLevel()).thenReturn(level);
    }

    @AfterEach
    void tearDown() {
        configManagerMock.close();
    }

    @Test
    void testGetValidServerPlayer_HrmDisabled_ReturnsNull() {
        when(mockConfig.isHrmEnabled()).thenReturn(false);

        ServerPlayer result = HrmUtil.getValidServerPlayer(event, db);
        assertNull(result);
    }

    @Test
    void testGetValidServerPlayer_NullDb_ReturnsNull() {
        when(mockConfig.isHrmEnabled()).thenReturn(true);

        ServerPlayer result = HrmUtil.getValidServerPlayer(event, null);
        assertNull(result);
    }

    @Test
    void testGetValidServerPlayer_ClientSide_ReturnsNull() {
        when(mockConfig.isHrmEnabled()).thenReturn(true);
        when(level.isClientSide()).thenReturn(true);

        ServerPlayer result = HrmUtil.getValidServerPlayer(event, db);
        assertNull(result);
    }

    @Test
    void testGetValidServerPlayer_NotServerPlayer_ReturnsNull() {
        when(mockConfig.isHrmEnabled()).thenReturn(true);
        when(level.isClientSide()).thenReturn(false);
        when(event.getEntity()).thenReturn(mock(Player.class));

        ServerPlayer result = HrmUtil.getValidServerPlayer(event, db);
        assertNull(result);
    }

    @Test
    void testGetValidServerPlayer_Valid_ReturnsServerPlayer() {
        when(mockConfig.isHrmEnabled()).thenReturn(true);
        when(level.isClientSide()).thenReturn(false);
        when(event.getEntity()).thenReturn(player);

        ServerPlayer result = HrmUtil.getValidServerPlayer(event, db);
        assertEquals(player, result);
    }
}
