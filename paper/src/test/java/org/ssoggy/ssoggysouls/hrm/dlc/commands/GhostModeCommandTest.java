package org.ssoggy.ssoggysouls.hrm.dlc.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.ssoggy.ssoggysouls.hrm.dlc.enums.GAMEMODESENUM;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStatic;
import org.ssoggy.ssoggysouls.util.TabCompleteUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GhostModeCommandTest {

    private static File testDir;
    private MockedStatic<Bukkit> bukkitMock;
    private MockedStatic<GAMEMODESENUM> gamemodesMock;
    private MockedStatic<TabCompleteUtil> tabCompleteUtilMock;

    private GhostModeCommand cmd;
    private Command command;

    @BeforeAll
    static void initGlobal() throws IOException {
        testDir = Files.createTempDirectory("ghostmodetest").toFile();
        JavaPlugin mockPlugin = mock(JavaPlugin.class);
        when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("test"));
        when(mockPlugin.getDataFolder()).thenReturn(testDir);
        RPStatic.CLIENT = mockPlugin;
    }

    @BeforeEach
    void setUp() {
        cmd = new GhostModeCommand();
        command = mock(Command.class);

        bukkitMock = Mockito.mockStatic(Bukkit.class);
        gamemodesMock = Mockito.mockStatic(GAMEMODESENUM.class);
        tabCompleteUtilMock = Mockito.mockStatic(TabCompleteUtil.class);
    }

    @AfterEach
    void tearDown() {
        if (bukkitMock != null) bukkitMock.close();
        if (gamemodesMock != null) gamemodesMock.close();
        if (tabCompleteUtilMock != null) tabCompleteUtilMock.close();
    }

    @Test
    void testCommandSenderSelfNoArgsSuccess() {
        Player senderPlayer = mock(Player.class);
        when(senderPlayer.getName()).thenReturn("SenderPlayer");

        OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
        when(offlinePlayer.getPlayer()).thenReturn(senderPlayer);

        bukkitMock.when(() -> Bukkit.getOfflinePlayer("SenderPlayer")).thenReturn(offlinePlayer);

        boolean result = cmd.onCommand(senderPlayer, command, "ghostmode", new String[0]);
        assertTrue(result);

        gamemodesMock.verify(() -> GAMEMODESENUM.setPlayerGameMode(senderPlayer, GAMEMODESENUM.GHOSTMODE));
        verify(senderPlayer).sendRichMessage(contains("Updated SenderPlayer gamemode to GhostMode!"));
    }

    @Test
    void testCommandWithArgTargetPlayerOnline() {
        CommandSender sender = mock(CommandSender.class);
        Player targetPlayer = mock(Player.class);
        when(targetPlayer.getName()).thenReturn("TargetPlayer");

        OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
        when(offlinePlayer.getPlayer()).thenReturn(targetPlayer);

        bukkitMock.when(() -> Bukkit.getOfflinePlayer("TargetPlayer")).thenReturn(offlinePlayer);

        boolean result = cmd.onCommand(sender, command, "ghostmode", new String[]{"TargetPlayer"});
        assertTrue(result);

        gamemodesMock.verify(() -> GAMEMODESENUM.setPlayerGameMode(targetPlayer, GAMEMODESENUM.GHOSTMODE));
        verify(sender).sendRichMessage(contains("Updated TargetPlayer gamemode to GhostMode!"));
    }

    @Test
    void testCommandTargetPlayerOffline() {
        CommandSender sender = mock(CommandSender.class);
        OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
        when(offlinePlayer.getPlayer()).thenReturn(null);

        bukkitMock.when(() -> Bukkit.getOfflinePlayer("OfflineUser")).thenReturn(offlinePlayer);

        boolean result = cmd.onCommand(sender, command, "ghostmode", new String[]{"OfflineUser"});
        assertTrue(result);

        gamemodesMock.verify(() -> GAMEMODESENUM.setPlayerGameMode(any(Player.class), any(GAMEMODESENUM.class)), never());
        verify(sender).sendRichMessage(contains("Something went wrong"));
    }

    @Test
    void testTabCompleteNoArgs() {
        tabCompleteUtilMock.when(() -> TabCompleteUtil.getOnlinePlayerNames(""))
                .thenReturn(List.of("Alice", "Bob"));

        List<String> completions = cmd.onTabComplete(mock(CommandSender.class), command, "ghostmode", new String[0]);
        assertEquals(List.of("Alice", "Bob"), completions);
    }

    @Test
    void testTabCompleteSingleArg() {
        tabCompleteUtilMock.when(() -> TabCompleteUtil.getOnlinePlayerNames("Al"))
                .thenReturn(List.of("Alice"));

        List<String> completions = cmd.onTabComplete(mock(CommandSender.class), command, "ghostmode", new String[]{"Al"});
        assertEquals(List.of("Alice"), completions);
    }

    @Test
    void testTabCompleteMultipleArgs() {
        List<String> completions = cmd.onTabComplete(mock(CommandSender.class), command, "ghostmode", new String[]{"Alice", "extra"});
        assertEquals(Collections.emptyList(), completions);
    }
}
