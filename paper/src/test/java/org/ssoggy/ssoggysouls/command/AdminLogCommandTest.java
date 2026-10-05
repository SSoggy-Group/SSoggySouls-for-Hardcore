package org.ssoggy.ssoggysouls.command;

import net.kyori.adventure.text.Component;
import org.bukkit.Server;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminLogCommandTest {

    @Mock
    private SSoggySouls plugin;
    @Mock
    private Server server;
    @Mock
    private BukkitScheduler scheduler;
    @Mock
    private Command command;
    @Mock
    private Logger logger;

    private MockedStatic<CommandUtil> commandUtilMock;

    private File tempFolder;
    private AdminLogCommand cmd;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() throws IOException {
        mocks = MockitoAnnotations.openMocks(this);
        tempFolder = Files.createTempDirectory("adminlogtest").toFile();

        when(plugin.getDataFolder()).thenReturn(tempFolder);
        when(plugin.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(scheduler);
        when(plugin.getLogger()).thenReturn(logger);
        when(plugin.isAdminLogAllowAll()).thenReturn(false);
        when(plugin.getAdminLogTrustedViewers()).thenReturn(Collections.emptySet());

        cmd = new AdminLogCommand(plugin);
        commandUtilMock = Mockito.mockStatic(CommandUtil.class);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(1);
            runnable.run();
            return null;
        }).when(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(1);
            runnable.run();
            return null;
        }).when(scheduler).runTask(eq(plugin), any(Runnable.class));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (commandUtilMock != null) commandUtilMock.close();
        if (mocks != null) mocks.close();
        File[] files = tempFolder.listFiles();
        if (files != null) {
            for (File f : files) f.delete();
        }
        tempFolder.delete();
    }

    @Test
    void testDisallowedPlayer() {
        Player player = mock(Player.class);
        when(player.isOp()).thenReturn(false);
        when(player.hasPermission("ssoggysouls.adminlog")).thenReturn(false);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("RegularPlayer");

        boolean result = cmd.onCommand(player, command, "adminlog", new String[0]);
        assertTrue(result);
        verify(player).sendMessage(contains("You don't have permission"));
    }

    @Test
    void testAllowedViaAdminLogAllowAll() {
        when(plugin.isAdminLogAllowAll()).thenReturn(true);
        Player player = mock(Player.class);

        boolean result = cmd.onCommand(player, command, "adminlog", new String[0]);
        assertTrue(result);
        verify(player, never()).sendMessage(contains("You don't have permission"));
    }

    @Test
    void testAllowedViaTrustedViewers() {
        UUID uuid = UUID.randomUUID();
        when(plugin.getAdminLogTrustedViewers()).thenReturn(Set.of(uuid.toString()));

        Player player = mock(Player.class);
        when(player.isOp()).thenReturn(false);
        when(player.hasPermission("ssoggysouls.adminlog")).thenReturn(false);
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getName()).thenReturn("TrustedPlayer");

        boolean result = cmd.onCommand(player, command, "adminlog", new String[0]);
        assertTrue(result);
        verify(player, never()).sendMessage(contains("You don't have permission"));
    }

    @Test
    void testAllowedViaTrustedViewersUsername() {
        when(plugin.getAdminLogTrustedViewers()).thenReturn(Set.of("trusteduser"));

        Player player = mock(Player.class);
        when(player.isOp()).thenReturn(false);
        when(player.hasPermission("ssoggysouls.adminlog")).thenReturn(false);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("TrustedUser");

        boolean result = cmd.onCommand(player, command, "adminlog", new String[0]);
        assertTrue(result);
        verify(player, never()).sendMessage(contains("You don't have permission"));
    }

    @Test
    void testInvalidLineCount() {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission("ssoggysouls.adminlog")).thenReturn(true);

        // Not a number
        boolean res1 = cmd.onCommand(sender, command, "adminlog", new String[]{"abc"});
        assertTrue(res1);
        commandUtilMock.verify(() -> CommandUtil.sendInteractiveUsage(eq(sender), contains("Invalid number"), any()));

        // Out of range <= 0
        boolean res2 = cmd.onCommand(sender, command, "adminlog", new String[]{"0"});
        assertTrue(res2);
        commandUtilMock.verify(() -> CommandUtil.sendInteractiveUsage(eq(sender), contains("between 1 and 100"), any()));

        // Out of range > 100
        boolean res3 = cmd.onCommand(sender, command, "adminlog", new String[]{"101"});
        assertTrue(res3);
    }

    @Test
    void testLogFileNotFound() {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission("ssoggysouls.adminlog")).thenReturn(true);

        boolean result = cmd.onCommand(sender, command, "adminlog", new String[0]);
        assertTrue(result);
        verify(sender).sendMessage(contains("No admin log file found"));
    }

    @Test
    void testSuccessfulDisplayConsole() throws IOException {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission("ssoggysouls.adminlog")).thenReturn(true);

        File logFile = new File(tempFolder, AdminLogger.LOG_FILE_NAME);
        List<String> logLines = List.of(
                "[2026-10-05 12:00:00] [INFO] ADMIN ACTION - Admin: revived Player1",
                "[2026-10-05 12:01:00] [INFO] Some other line without action"
        );
        Files.write(logFile.toPath(), logLines);

        boolean result = cmd.onCommand(sender, command, "adminlog", new String[]{"10"});
        assertTrue(result);
        verify(sender).sendMessage(contains("Admin Action Log"));
        verify(sender).sendMessage(contains("══════════════════════"));
    }

    @Test
    void testSuccessfulDisplayPlayerInteractive() throws IOException {
        Player player = mock(Player.class);
        when(player.hasPermission("ssoggysouls.adminlog")).thenReturn(true);

        File logFile = new File(tempFolder, AdminLogger.LOG_FILE_NAME);
        List<String> logLines = List.of(
                "[2026-10-05 12:00:00] [INFO] ADMIN ACTION - Admin: revived Player1"
        );
        Files.write(logFile.toPath(), logLines);

        boolean result = cmd.onCommand(player, command, "adminlog", new String[0]);
        assertTrue(result);
        verify(player).sendMessage(any(Component.class));
    }

    @Test
    void testSuccessfulDisplayEmptyLog() throws IOException {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission("ssoggysouls.adminlog")).thenReturn(true);

        File logFile = new File(tempFolder, AdminLogger.LOG_FILE_NAME);
        Files.write(logFile.toPath(), Collections.emptyList());

        boolean result = cmd.onCommand(sender, command, "adminlog", new String[0]);
        assertTrue(result);
        verify(sender).sendMessage(contains("(Empty)"));
    }

    @Test
    void testReadError() {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission("ssoggysouls.adminlog")).thenReturn(true);

        File dir = new File(tempFolder, AdminLogger.LOG_FILE_NAME);
        dir.mkdir();

        boolean result = cmd.onCommand(sender, command, "adminlog", new String[0]);
        assertTrue(result);
        verify(sender).sendMessage(contains("Failed to read admin logs"));
    }
}
