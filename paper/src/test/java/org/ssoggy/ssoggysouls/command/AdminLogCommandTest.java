package org.ssoggy.ssoggysouls.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ssoggy.ssoggysouls.SSoggySouls;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AdminLogCommandTest {

    @Mock
    private SSoggySouls plugin;
    @Mock
    private CommandSender sender;
    @Mock
    private Player player;
    @Mock
    private Command command;

    private AdminLogCommand adminLogCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminLogCommand = new AdminLogCommand(plugin);
        when(plugin.isAdminLogAllowAll()).thenReturn(true);
        when(plugin.getDataFolder()).thenReturn(new File("."));
    }

    @Test
    void testDisplayLogEntriesConsole() {
        Deque<String> lines = new ArrayDeque<>();
        lines.add("[2024-06-14 12:00] Admin did something");

        adminLogCommand.displayLogEntries(sender, lines);
        verify(sender, atLeastOnce()).sendMessage(anyString());
    }

    @Test
    void testDisplayLogEntriesPlayer() {
        Deque<String> lines = new ArrayDeque<>();
        lines.add("[2024-06-14 12:00] Admin did something");

        adminLogCommand.displayLogEntries(player, lines);
        verify(player, atLeastOnce()).sendMessage((net.kyori.adventure.text.Component) any());
    }
}
