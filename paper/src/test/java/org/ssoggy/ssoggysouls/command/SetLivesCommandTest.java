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
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.util.CommandUtil;
import org.ssoggy.ssoggysouls.util.MessageUtil;
import org.ssoggy.ssoggysouls.util.PermissionUtil;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SetLivesCommandTest {

    private SSoggySouls plugin;
    private DatabaseManager db;
    private SetLivesCommand command;
    private CommandSender sender;
    private Command mockCommand;
    private MockedStatic<CommandUtil> mockedCommandUtil;
    private MockedStatic<MessageUtil> mockedMessageUtil;
    private MockedStatic<PermissionUtil> mockedPermissionUtil;

    @BeforeEach
    void setUp() {
        plugin = mock(SSoggySouls.class);
        db = mock(DatabaseManager.class);
        when(plugin.getDatabaseManager()).thenReturn(db);

        command = new SetLivesCommand(plugin);
        sender = mock(Player.class);
        mockCommand = mock(Command.class);

        mockedCommandUtil = Mockito.mockStatic(CommandUtil.class);
        mockedMessageUtil = Mockito.mockStatic(MessageUtil.class);
        mockedPermissionUtil = Mockito.mockStatic(PermissionUtil.class);

        mockedCommandUtil.when(() -> CommandUtil.checkPermission(any(), anyString())).thenReturn(true);
        mockedPermissionUtil.when(() -> PermissionUtil.isBlockedByLimboOpSecurity(any(), any())).thenReturn(false);
    }

    @AfterEach
    void tearDown() {
        mockedCommandUtil.close();
        mockedMessageUtil.close();
        mockedPermissionUtil.close();
    }

    @Test
    void testNegativeLives_UsesInteractiveUsage() {
        String[] args = {"PlayerName", "-5"};

        boolean result = command.onCommand(sender, mockCommand, "psetlives", args);

        assertTrue(result);
        mockedCommandUtil.verify(() -> CommandUtil.sendInteractiveUsage(sender, "&cLives cannot be negative.", "/psetlives PlayerName "));
    }

    @Test
    void testOverMaxLives_UsesInteractiveUsage() {
        when(plugin.getMaxLives()).thenReturn(10);
        String[] args = {"PlayerName", "15"};

        boolean result = command.onCommand(sender, mockCommand, "psetlives", args);

        assertTrue(result);
        mockedCommandUtil.verify(() -> CommandUtil.sendInteractiveUsage(sender, "&cMaximum lives: 10", "/psetlives PlayerName "));
    }
}
