package org.ssoggy.ssoggysouls.command;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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
import org.ssoggy.ssoggysouls.util.AdminLogger;
import org.ssoggy.ssoggysouls.util.CommandUtil;
import org.ssoggy.ssoggysouls.util.PermissionUtil;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SetLimboSpawnCommandTest {

    @Mock
    private SSoggySouls plugin;
    @Mock
    private Command command;
    @Mock
    private BukkitScheduler scheduler;

    private MockedStatic<Bukkit> bukkitMock;
    private MockedStatic<CommandUtil> commandUtilMock;
    private MockedStatic<PermissionUtil> permissionUtilMock;
    private MockedStatic<AdminLogger> adminLoggerMock;

    private SetLimboSpawnCommand cmd;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        cmd = new SetLimboSpawnCommand(plugin);

        bukkitMock = Mockito.mockStatic(Bukkit.class);
        bukkitMock.when(Bukkit::getScheduler).thenReturn(scheduler);

        commandUtilMock = Mockito.mockStatic(CommandUtil.class);
        permissionUtilMock = Mockito.mockStatic(PermissionUtil.class);
        adminLoggerMock = Mockito.mockStatic(AdminLogger.class);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(1);
            runnable.run();
            return null;
        }).when(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (bukkitMock != null) {
            bukkitMock.close();
        }
        if (commandUtilMock != null) {
            commandUtilMock.close();
        }
        if (permissionUtilMock != null) {
            permissionUtilMock.close();
        }
        if (adminLoggerMock != null) {
            adminLoggerMock.close();
        }
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    void testNullSender() {
        assertFalse(cmd.onCommand(null, command, "setlimbospawn", new String[0]));
    }

    @Test
    void testNoPermission() {
        CommandSender sender = mock(CommandSender.class);
        commandUtilMock.when(() -> CommandUtil.checkPermission(sender, "ssoggysouls.admin")).thenReturn(false);

        boolean result = cmd.onCommand(sender, command, "setlimbospawn", new String[0]);
        assertTrue(result);
        verify(plugin, never()).saveLimboSpawn(any());
    }

    @Test
    void testBlockedByLimboSecurity() {
        CommandSender sender = mock(CommandSender.class);
        commandUtilMock.when(() -> CommandUtil.checkPermission(sender, "ssoggysouls.admin")).thenReturn(true);
        permissionUtilMock.when(() -> PermissionUtil.isBlockedByLimboOpSecurity(sender, plugin)).thenReturn(true);

        boolean result = cmd.onCommand(sender, command, "setlimbospawn", new String[0]);
        assertTrue(result);
        permissionUtilMock.verify(() -> PermissionUtil.sendSecurityBlockMessage(sender));
        verify(plugin, never()).saveLimboSpawn(any());
    }

    @Test
    void testSenderNotPlayer() {
        CommandSender sender = mock(CommandSender.class);
        commandUtilMock.when(() -> CommandUtil.checkPermission(sender, "ssoggysouls.admin")).thenReturn(true);
        permissionUtilMock.when(() -> PermissionUtil.isBlockedByLimboOpSecurity(sender, plugin)).thenReturn(false);

        boolean result = cmd.onCommand(sender, command, "setlimbospawn", new String[0]);
        assertFalse(result);
        verify(sender).sendMessage(any(String.class));
        verify(plugin, never()).saveLimboSpawn(any());
    }

    @Test
    void testPlayerLocationNull() {
        Player player = mock(Player.class);
        commandUtilMock.when(() -> CommandUtil.checkPermission(player, "ssoggysouls.admin")).thenReturn(true);
        permissionUtilMock.when(() -> PermissionUtil.isBlockedByLimboOpSecurity(player, plugin)).thenReturn(false);
        when(player.getLocation()).thenReturn(null);

        boolean result = cmd.onCommand(player, command, "setlimbospawn", new String[0]);
        assertFalse(result);
        verify(plugin, never()).saveLimboSpawn(any());
    }

    @Test
    void testSuccessfulSpawnSet() {
        Player player = mock(Player.class);
        World world = mock(World.class);
        when(world.getName()).thenReturn("limbo_world");

        Location loc = new Location(world, 10.5, 64.0, -20.5);
        when(player.getLocation()).thenReturn(loc);
        when(player.getName()).thenReturn("AdminPlayer");

        commandUtilMock.when(() -> CommandUtil.checkPermission(player, "ssoggysouls.admin")).thenReturn(true);
        permissionUtilMock.when(() -> PermissionUtil.isBlockedByLimboOpSecurity(player, plugin)).thenReturn(false);

        boolean result = cmd.onCommand(player, command, "setlimbospawn", new String[0]);
        assertTrue(result);

        verify(plugin).saveLimboSpawn(loc);
        verify(player).sendMessage(any(String.class));
        adminLoggerMock.verify(() -> AdminLogger.log(eq(plugin), eq("AdminPlayer"), contains("limbo_world")));
    }

    @Test
    void testSuccessfulSpawnSetWithNullWorld() {
        Player player = mock(Player.class);
        Location loc = new Location(null, 1.0, 2.0, 3.0);
        when(player.getLocation()).thenReturn(loc);
        when(player.getName()).thenReturn("AdminPlayer");

        commandUtilMock.when(() -> CommandUtil.checkPermission(player, "ssoggysouls.admin")).thenReturn(true);
        permissionUtilMock.when(() -> PermissionUtil.isBlockedByLimboOpSecurity(player, plugin)).thenReturn(false);

        boolean result = cmd.onCommand(player, command, "setlimbospawn", new String[0]);
        assertTrue(result);

        verify(plugin).saveLimboSpawn(loc);
        adminLoggerMock.verify(() -> AdminLogger.log(eq(plugin), eq("AdminPlayer"), contains("unknown")));
    }
}
