package org.ssoggy.ssoggysouls.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ssoggy.ssoggysouls.SSoggySouls;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.model.PlayerData;
import org.ssoggy.ssoggysouls.util.AdminLogger;
import org.ssoggy.ssoggysouls.util.CommandUtil;
import org.ssoggy.ssoggysouls.util.MessageUtil;
import org.ssoggy.ssoggysouls.util.PermissionUtil;
import org.ssoggy.ssoggysouls.util.TabCompleteUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    @Mock
    private BukkitScheduler scheduler;

    private MockedStatic<CommandUtil> commandUtilMock;
    private MockedStatic<PermissionUtil> permissionUtilMock;
    private MockedStatic<TabCompleteUtil> tabCompleteUtilMock;
    private MockedStatic<AdminLogger> adminLoggerMock;
    private MockedStatic<Bukkit> bukkitMock;

    private SetLivesCommand setLivesCommand;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        when(plugin.getDatabaseManager()).thenReturn(db);
        when(plugin.getMaxLives()).thenReturn(10);
        setLivesCommand = new SetLivesCommand(plugin);

        commandUtilMock = Mockito.mockStatic(CommandUtil.class);
        commandUtilMock.when(() -> CommandUtil.checkPermission(any(), any())).thenReturn(true);

        permissionUtilMock = Mockito.mockStatic(PermissionUtil.class);
        permissionUtilMock.when(() -> PermissionUtil.isBlockedByLimboOpSecurity(any(), any())).thenReturn(false);

        tabCompleteUtilMock = Mockito.mockStatic(TabCompleteUtil.class);
        adminLoggerMock = Mockito.mockStatic(AdminLogger.class);

        bukkitMock = Mockito.mockStatic(Bukkit.class);
        bukkitMock.when(Bukkit::getScheduler).thenReturn(scheduler);

        // Execute async scheduler task synchronously for testing
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(1);
            runnable.run();
            return null;
        }).when(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (commandUtilMock != null) {
            commandUtilMock.close();
        }
        if (permissionUtilMock != null) {
            permissionUtilMock.close();
        }
        if (tabCompleteUtilMock != null) {
            tabCompleteUtilMock.close();
        }
        if (adminLoggerMock != null) {
            adminLoggerMock.close();
        }
        if (bukkitMock != null) {
            bukkitMock.close();
        }
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    void testNoPermission() {
        commandUtilMock.when(() -> CommandUtil.checkPermission(sender, "ssoggysouls.admin")).thenReturn(false);

        boolean result = setLivesCommand.onCommand(sender, command, "psetlives", new String[]{"PlayerName", "3"});

        assertTrue(result, "Command should return true when permission check fails");
        permissionUtilMock.verify(() -> PermissionUtil.isBlockedByLimboOpSecurity(any(), any()), never());
    }

    @Test
    void testBlockedByLimboOpSecurity() {
        permissionUtilMock.when(() -> PermissionUtil.isBlockedByLimboOpSecurity(sender, plugin)).thenReturn(true);

        boolean result = setLivesCommand.onCommand(sender, command, "psetlives", new String[]{"PlayerName", "3"});

        assertTrue(result, "Command should return true when blocked by Limbo OP security");
        permissionUtilMock.verify(() -> PermissionUtil.sendSecurityBlockMessage(sender));
    }

    @Test
    void testInvalidArgsCount() {
        boolean resultZero = setLivesCommand.onCommand(sender, command, "psetlives", new String[]{});
        boolean resultOne = setLivesCommand.onCommand(sender, command, "psetlives", new String[]{"PlayerName"});
        boolean resultThree = setLivesCommand.onCommand(sender, command, "psetlives", new String[]{"PlayerName", "3", "extra"});

        assertFalse(resultZero);
        assertFalse(resultOne);
        assertFalse(resultThree);

        commandUtilMock.verify(() -> CommandUtil.sendInteractiveUsage(
                eq(sender),
                eq("&cUsage: /psetlives <player> <lives>"),
                eq("/psetlives ")
        ), Mockito.times(3));
    }

    @Test
    void testInvalidNumberFormat() {
        String[] args = {"PlayerName", "invalid_number"};

        boolean result = setLivesCommand.onCommand(sender, command, "psetlives", args);

        assertFalse(result, "Command should return false for invalid number format");
        commandUtilMock.verify(() -> CommandUtil.sendInteractiveUsage(
                eq(sender),
                eq("&cInvalid number: invalid_number"),
                eq("/psetlives PlayerName ")
        ));
    }

    @Test
    void testNegativeLives() {
        String[] args = {"PlayerName", "-1"};

        boolean result = setLivesCommand.onCommand(sender, command, "psetlives", args);

        assertFalse(result, "Command should return false for negative lives");
        commandUtilMock.verify(() -> CommandUtil.sendInteractiveUsage(
                eq(sender),
                eq("&cLives cannot be negative."),
                eq("/psetlives PlayerName ")
        ));
    }

    @Test
    void testExceedsMaxLives() {
        String[] args = {"PlayerName", "15"};

        boolean result = setLivesCommand.onCommand(sender, command, "psetlives", args);

        assertFalse(result, "Command should return false when exceeding max lives");
        commandUtilMock.verify(() -> CommandUtil.sendInteractiveUsage(
                eq(sender),
                eq("&cMaximum lives: 10"),
                eq("/psetlives PlayerName ")
        ));
    }

    @Test
    void testPlayerNotFound() {
        when(db.getPlayerByName("UnknownPlayer")).thenReturn(null);

        boolean result = setLivesCommand.onCommand(sender, command, "psetlives", new String[]{"UnknownPlayer", "5"});

        assertTrue(result);
        verify(sender).sendMessage(MessageUtil.get("revive-player-not-found", "player", "UnknownPlayer"));
        verify(db, never()).setLives(any(), any(Integer.class));
    }

    @Test
    void testSetLivesSuccess() {
        UUID targetUuid = UUID.randomUUID();
        PlayerData data = mock(PlayerData.class);
        when(data.getUuid()).thenReturn(targetUuid);
        when(data.getUsername()).thenReturn("TargetUser");
        when(db.getPlayerByName("TargetUser")).thenReturn(data);
        when(sender.getName()).thenReturn("AdminUser");

        boolean result = setLivesCommand.onCommand(sender, command, "psetlives", new String[]{"TargetUser", "5"});

        assertTrue(result);
        verify(db).setLives(targetUuid, 5);
        adminLoggerMock.verify(() -> AdminLogger.log(plugin, "AdminUser", "set TargetUser's lives to 5"));
        verify(sender).sendMessage(MessageUtil.get("lives-set", "player", "TargetUser", "lives", 5));
    }

    @Test
    void testOnTabComplete() {
        tabCompleteUtilMock.when(() -> TabCompleteUtil.getOnlinePlayerNames("Tar"))
                .thenReturn(Collections.singletonList("TargetUser"));

        List<String> arg1Completions = setLivesCommand.onTabComplete(sender, command, "psetlives", new String[]{"Tar"});
        assertEquals(Collections.singletonList("TargetUser"), arg1Completions);

        List<String> arg2Completions = setLivesCommand.onTabComplete(sender, command, "psetlives", new String[]{"TargetUser", ""});
        assertEquals(Arrays.asList("1", "2", "3", "5"), arg2Completions);

        List<String> arg3Completions = setLivesCommand.onTabComplete(sender, command, "psetlives", new String[]{"TargetUser", "1", "extra"});
        assertEquals(Collections.emptyList(), arg3Completions);
    }
}
