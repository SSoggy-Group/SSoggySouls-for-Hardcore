package org.ssoggy.ssoggysouls.command;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ssoggy.ssoggysouls.SSoggySouls;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.util.CommandUtil;
import org.ssoggy.ssoggysouls.util.PermissionUtil;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class SetLivesCommandTest {

    @Mock
    private SSoggySouls plugin;
    @Mock
    private DatabaseManager db;
    @Mock
    private Player sender;
    @Mock
    private Command command;

    private MockedStatic<CommandUtil> commandUtilMock;
    private MockedStatic<PermissionUtil> permissionUtilMock;
    private SetLivesCommand setLivesCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(plugin.getDatabaseManager()).thenReturn(db);
        setLivesCommand = new SetLivesCommand(plugin);

        commandUtilMock = Mockito.mockStatic(CommandUtil.class);
        commandUtilMock.when(() -> CommandUtil.checkPermission(any(), any())).thenReturn(true);

        permissionUtilMock = Mockito.mockStatic(PermissionUtil.class);
        permissionUtilMock.when(() -> PermissionUtil.isBlockedByLimboOpSecurity(any(), any())).thenReturn(false);
    }

    @AfterEach
    void tearDown() {
        if (commandUtilMock != null) {
            commandUtilMock.close();
        }
        if (permissionUtilMock != null) {
            permissionUtilMock.close();
        }
    }

    @Test
    void testInvalidNumberFormat() {
        // Prepare arguments with an invalid number
        String[] args = {"PlayerName", "invalid_number"};

        // Execute the command
        boolean result = setLivesCommand.onCommand(sender, command, "psetlives", args);

        // Verify the result is false
        assertFalse(result, "Command should return false for invalid number format");

        // Verify that CommandUtil.sendInteractiveUsage was called with correct arguments
        commandUtilMock.verify(() -> CommandUtil.sendInteractiveUsage(
                eq(sender),
                eq("&cInvalid number: invalid_number"),
                eq("/psetlives PlayerName ")
        ));
    }
}
