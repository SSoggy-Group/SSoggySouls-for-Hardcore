package org.ssoggy.ssoggysouls.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
import org.ssoggy.ssoggysouls.util.PlayerRevivalUtil;
import org.ssoggy.ssoggysouls.util.TabCompleteUtil;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviveCommandTest {

    @Mock
    private SSoggySouls plugin;
    @Mock
    private DatabaseManager db;
    @Mock
    private Command command;
    @Mock
    private BukkitScheduler scheduler;
    @Mock
    private CommandSender sender;

    private MockedStatic<Bukkit> bukkitMock;
    private MockedStatic<CommandUtil> commandUtilMock;
    private MockedStatic<PermissionUtil> permissionUtilMock;
    private MockedStatic<PlayerRevivalUtil> playerRevivalUtilMock;
    private MockedStatic<AdminLogger> adminLoggerMock;
    private MockedStatic<TabCompleteUtil> tabCompleteUtilMock;

    private ReviveCommand cmd;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        when(plugin.getDatabaseManager()).thenReturn(db);
        when(plugin.getLivesOnRevive()).thenReturn(1);
        when(sender.getName()).thenReturn("Admin");
        cmd = new ReviveCommand(plugin);

        bukkitMock = Mockito.mockStatic(Bukkit.class);
        bukkitMock.when(Bukkit::getScheduler).thenReturn(scheduler);

        commandUtilMock = Mockito.mockStatic(CommandUtil.class);
        permissionUtilMock = Mockito.mockStatic(PermissionUtil.class);
        playerRevivalUtilMock = Mockito.mockStatic(PlayerRevivalUtil.class);
        adminLoggerMock = Mockito.mockStatic(AdminLogger.class);
        tabCompleteUtilMock = Mockito.mockStatic(TabCompleteUtil.class);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(1);
            runnable.run();
            return null;
        }).when(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (bukkitMock != null) bukkitMock.close();
        if (commandUtilMock != null) commandUtilMock.close();
        if (permissionUtilMock != null) permissionUtilMock.close();
        if (playerRevivalUtilMock != null) playerRevivalUtilMock.close();
        if (adminLoggerMock != null) adminLoggerMock.close();
        if (tabCompleteUtilMock != null) tabCompleteUtilMock.close();
        if (mocks != null) mocks.close();
    }

    @Test
    void testNoPermission() {
        commandUtilMock.when(() -> CommandUtil.checkPermission(eq(sender), eq("ssoggysouls.revive"), anyString())).thenReturn(false);

        boolean result = cmd.onCommand(sender, command, "revive", new String[]{"player"});
        assertTrue(result);
        verify(scheduler, never()).runTaskAsynchronously(any(), any(Runnable.class));
    }

    @Test
    void testBlockedByLimboSecurity() {
        commandUtilMock.when(() -> CommandUtil.checkPermission(eq(sender), eq("ssoggysouls.revive"), anyString())).thenReturn(true);
        permissionUtilMock.when(() -> PermissionUtil.isBlockedByLimboOpSecurity(sender, plugin)).thenReturn(true);

        boolean result = cmd.onCommand(sender, command, "revive", new String[]{"player"});
        assertTrue(result);
        permissionUtilMock.verify(() -> PermissionUtil.sendSecurityBlockMessage(sender));
        verify(scheduler, never()).runTaskAsynchronously(any(), any(Runnable.class));
    }

    @Test
    void testInvalidArgs() {
        commandUtilMock.when(() -> CommandUtil.checkPermission(eq(sender), eq("ssoggysouls.revive"), anyString())).thenReturn(true);
        permissionUtilMock.when(() -> PermissionUtil.isBlockedByLimboOpSecurity(sender, plugin)).thenReturn(false);

        boolean result = cmd.onCommand(sender, command, "revive", new String[0]);
        assertFalse(result);
        commandUtilMock.verify(() -> CommandUtil.sendInteractiveUsage(sender, "&cUsage: /revive <player>", "/revive "));
    }

    @Test
    void testPlayerNotFound() {
        commandUtilMock.when(() -> CommandUtil.checkPermission(eq(sender), eq("ssoggysouls.revive"), anyString())).thenReturn(true);
        permissionUtilMock.when(() -> PermissionUtil.isBlockedByLimboOpSecurity(sender, plugin)).thenReturn(false);
        when(db.getPlayerByName("Missing")).thenReturn(null);

        boolean result = cmd.onCommand(sender, command, "revive", new String[]{"Missing"});
        assertTrue(result);
        verify(sender).sendMessage(MessageUtil.get("revive-player-not-found", "player", "Missing"));
    }

    @Test
    void testPlayerNotDead() {
        commandUtilMock.when(() -> CommandUtil.checkPermission(eq(sender), eq("ssoggysouls.revive"), anyString())).thenReturn(true);
        permissionUtilMock.when(() -> PermissionUtil.isBlockedByLimboOpSecurity(sender, plugin)).thenReturn(false);

        PlayerData data = mock(PlayerData.class);
        when(data.getUsername()).thenReturn("AliveUser");
        when(data.isDead()).thenReturn(false);
        when(db.getPlayerByName("AliveUser")).thenReturn(data);

        boolean result = cmd.onCommand(sender, command, "revive", new String[]{"AliveUser"});
        assertTrue(result);
        verify(sender).sendMessage(MessageUtil.get("revive-not-dead", "player", "AliveUser"));
    }

    @Test
    void testReviveSuccess() {
        commandUtilMock.when(() -> CommandUtil.checkPermission(eq(sender), eq("ssoggysouls.revive"), anyString())).thenReturn(true);
        permissionUtilMock.when(() -> PermissionUtil.isBlockedByLimboOpSecurity(sender, plugin)).thenReturn(false);

        UUID uuid = UUID.randomUUID();
        PlayerData data = mock(PlayerData.class);
        when(data.getUuid()).thenReturn(uuid);
        when(data.getUsername()).thenReturn("DeadUser");
        when(data.isDead()).thenReturn(true);
        when(db.getPlayerByName("DeadUser")).thenReturn(data);
        when(db.revivePlayer(uuid, 1)).thenReturn(true);

        boolean result = cmd.onCommand(sender, command, "revive", new String[]{"DeadUser"});
        assertTrue(result);

        adminLoggerMock.verify(() -> AdminLogger.log(plugin, "Admin", "revived DeadUser (lives: 1)"));
        verify(sender).sendMessage(MessageUtil.get("revive-admin-success", "player", "DeadUser", "lives", 1));
        playerRevivalUtilMock.verify(() -> PlayerRevivalUtil.restoreOnlineSpectator(plugin, data));
        verify(plugin).removeDroppedHeads(uuid);
    }

    @Test
    void testReviveDbFailure() {
        commandUtilMock.when(() -> CommandUtil.checkPermission(eq(sender), eq("ssoggysouls.revive"), anyString())).thenReturn(true);
        permissionUtilMock.when(() -> PermissionUtil.isBlockedByLimboOpSecurity(sender, plugin)).thenReturn(false);

        UUID uuid = UUID.randomUUID();
        PlayerData data = mock(PlayerData.class);
        when(data.getUuid()).thenReturn(uuid);
        when(data.getUsername()).thenReturn("DeadUser");
        when(data.isDead()).thenReturn(true);
        when(db.getPlayerByName("DeadUser")).thenReturn(data);
        when(db.revivePlayer(uuid, 1)).thenReturn(false);

        boolean result = cmd.onCommand(sender, command, "revive", new String[]{"DeadUser"});
        assertTrue(result);

        verify(sender).sendMessage(MessageUtil.colorize("&cFailed to revive DeadUser. Check console for errors."));
    }

    @Test
    void testTabComplete() {
        tabCompleteUtilMock.when(() -> TabCompleteUtil.getOnlinePlayerNames("de"))
                .thenReturn(List.of("DeadUser"));

        List<String> result1 = cmd.onTabComplete(sender, command, "revive", new String[]{"de"});
        assertEquals(List.of("DeadUser"), result1);

        List<String> result2 = cmd.onTabComplete(sender, command, "revive", new String[]{"de", "extra"});
        assertEquals(Collections.emptyList(), result2);
    }
}
