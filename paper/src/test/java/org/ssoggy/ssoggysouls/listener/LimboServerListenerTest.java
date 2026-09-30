package org.ssoggy.ssoggysouls.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.ssoggy.ssoggysouls.SSoggySouls;
import org.ssoggy.ssoggysouls.task.LimboCheckTask;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LimboServerListenerTest {

    private MockedStatic<Bukkit> mockedBukkit;
    private SSoggySouls plugin;
    private LimboCheckTask checkTask;
    private LimboServerListener listener;

    @BeforeEach
    void setUp() {
        mockedBukkit = mockStatic(Bukkit.class);
        mockedBukkit.when(Bukkit::getItemFactory).thenReturn(mock(org.bukkit.inventory.ItemFactory.class));
        plugin = mock(SSoggySouls.class);
        checkTask = mock(LimboCheckTask.class);
        listener = new LimboServerListener(plugin, checkTask);
    }

    @AfterEach
    void tearDown() {
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
    }

    @Test
    void testCommandPreprocessVisitorAllowed() {
        Player player = mock(Player.class);
        when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);
        when(player.hasPermission(anyString())).thenReturn(false);

        PlayerCommandPreprocessEvent event = mock(PlayerCommandPreprocessEvent.class);
        when(event.getPlayer()).thenReturn(player);
        when(event.getMessage()).thenReturn("/spawn");

        listener.onCommandPreprocess(event);

        verify(event, never()).setCancelled(true);
    }

    @Test
    void testCommandPreprocessDeadPlayerCancelled() {
        Player player = mock(Player.class);
        when(player.getGameMode()).thenReturn(GameMode.ADVENTURE);
        when(player.hasPermission(anyString())).thenReturn(false);

        PlayerCommandPreprocessEvent event = mock(PlayerCommandPreprocessEvent.class);
        when(event.getPlayer()).thenReturn(player);
        when(event.getMessage()).thenReturn("/spawn");

        listener.onCommandPreprocess(event);

        verify(event).setCancelled(true);
    }

    @Test
    void testCommandPreprocessWhitelistedCommandAllowed() {
        Player player = mock(Player.class);
        when(player.getGameMode()).thenReturn(GameMode.ADVENTURE);
        when(player.hasPermission(anyString())).thenReturn(false);

        PlayerCommandPreprocessEvent event = mock(PlayerCommandPreprocessEvent.class);
        when(event.getPlayer()).thenReturn(player);
        when(event.getMessage()).thenReturn("/msg hello");

        listener.onCommandPreprocess(event);

        verify(event, never()).setCancelled(true);
    }
}
