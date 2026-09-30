package org.ssoggy.ssoggysouls.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ssoggy.ssoggysouls.SSoggySouls;
import org.ssoggy.ssoggysouls.task.LimboCheckTask;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LimboServerListenerTest {

    private SSoggySouls plugin;
    private LimboCheckTask checkTask;
    private LimboServerListener listener;

    @BeforeEach
    void setUp() {
        plugin = mock(SSoggySouls.class);
        checkTask = mock(LimboCheckTask.class);
        listener = new LimboServerListener(plugin, checkTask);
    }

    @Test
    void testCommandPreprocessVisitorAllowed() {
        Player player = mock(Player.class);
        when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);
        when(player.hasPermission(anyString())).thenReturn(false);

        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, "/spawn");
        listener.onCommandPreprocess(event);

        assertFalse(event.isCancelled(), "Visitor command should not be cancelled");
        verify(player, never()).performCommand(anyString());
    }

    @Test
    void testCommandPreprocessDeadPlayerCancelled() {
        Player player = mock(Player.class);
        when(player.getGameMode()).thenReturn(GameMode.ADVENTURE);
        when(player.hasPermission(anyString())).thenReturn(false);

        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, "/spawn");
        listener.onCommandPreprocess(event);

        assertTrue(event.isCancelled(), "Dead player command should be cancelled");
        verify(player, never()).performCommand(anyString());
    }

    @Test
    void testCommandPreprocessWhitelistedCommandAllowed() {
        Player player = mock(Player.class);
        when(player.getGameMode()).thenReturn(GameMode.ADVENTURE);
        when(player.hasPermission(anyString())).thenReturn(false);

        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, "/msg hello");
        listener.onCommandPreprocess(event);

        assertFalse(event.isCancelled(), "Whitelisted command should not be cancelled even for dead players");
        verify(player, never()).performCommand(anyString());
    }
}
