package org.ssoggy.ssoggysouls.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.ssoggy.ssoggysouls.SSoggySouls;
import org.ssoggy.ssoggysouls.util.CommandUtil;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminLogCommandTest {

    private SSoggySouls plugin;
    private AdminLogCommand command;
    private CommandSender sender;
    private Command mockCommand;
    private MockedStatic<CommandUtil> mockedCommandUtil;

    @BeforeEach
    void setUp() {
        plugin = mock(SSoggySouls.class);
        command = new AdminLogCommand(plugin);
        sender = mock(Player.class);
        mockCommand = mock(Command.class);
        when(plugin.isAdminLogAllowAll()).thenReturn(true);

        mockedCommandUtil = Mockito.mockStatic(CommandUtil.class);
    }

    @AfterEach
    void tearDown() {
        mockedCommandUtil.close();
    }

    @Test
    void testNegativeLineCount_UsesInteractiveUsage() {
        String[] args = {"-5"};

        boolean result = command.onCommand(sender, mockCommand, "adminlog", args);

        assertTrue(result);
        mockedCommandUtil.verify(() -> CommandUtil.sendInteractiveUsage(sender, "&cPlease specify a number between 1 and 100.", "/adminlog "));
    }

    @Test
    void testOverMaxLineCount_UsesInteractiveUsage() {
        String[] args = {"150"};

        boolean result = command.onCommand(sender, mockCommand, "adminlog", args);

        assertTrue(result);
        mockedCommandUtil.verify(() -> CommandUtil.sendInteractiveUsage(sender, "&cPlease specify a number between 1 and 100.", "/adminlog "));
    }
}
