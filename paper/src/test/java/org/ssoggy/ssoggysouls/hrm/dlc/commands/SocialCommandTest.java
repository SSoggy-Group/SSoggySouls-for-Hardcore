package org.ssoggy.ssoggysouls.hrm.dlc.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStatic;
import org.ssoggy.ssoggysouls.util.MessageUtil;
import org.ssoggy.ssoggysouls.util.TabCompleteUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SocialCommandTest {

    private File tempDir;
    private MockedStatic<Bukkit> bukkitMock;
    private MockedStatic<TabCompleteUtil> tabCompleteUtilMock;
    private SocialCommand command;
    private Command mockCmd;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("socialcmdtest").toFile();
        JavaPlugin mockPlugin = mock(JavaPlugin.class);
        org.bukkit.Server mockServer = mock(org.bukkit.Server.class);
        when(mockPlugin.getServer()).thenReturn(mockServer);
        when(mockPlugin.getDataFolder()).thenReturn(tempDir);
        when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("test"));
        RPStatic.init(mockPlugin);

        bukkitMock = Mockito.mockStatic(Bukkit.class);
        tabCompleteUtilMock = Mockito.mockStatic(TabCompleteUtil.class);

        command = new SocialCommand();
        mockCmd = mock(Command.class);
    }

    @AfterEach
    void tearDown() {
        if (bukkitMock != null) bukkitMock.close();
        if (tabCompleteUtilMock != null) tabCompleteUtilMock.close();
        if (RPStatic.SOCIAL_STORAGE != null) RPStatic.SOCIAL_STORAGE.shutdown();
        if (RPStatic.USERNAME_CACHE != null) RPStatic.USERNAME_CACHE.shutdown();
        if (RPStatic.DEAD_STORAGE != null) RPStatic.DEAD_STORAGE.shutdown();
        if (RPStatic.STATS_STORAGE != null) RPStatic.STATS_STORAGE.shutdown();

        File[] files = tempDir.listFiles();
        if (files != null) {
            for (File f : files) f.delete();
        }
        tempDir.delete();
    }

    @Test
    void testCommandNoArgs() {
        CommandSender sender = mock(CommandSender.class);
        boolean result = command.onCommand(sender, mockCmd, "trust", new String[0]);
        assertTrue(result);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sender).sendRichMessage(captor.capture());
        assertTrue(captor.getValue().contains("/trust"));
    }

    @Test
    void testConsoleSender() {
        CommandSender sender = mock(CommandSender.class);
        boolean result = command.onCommand(sender, mockCmd, "trust", new String[]{"info"});
        assertTrue(result);
        verify(sender).sendMessage(MessageUtil.get("command-only-players"));
    }

    @Test
    void testSelfTargetFails() {
        Player player = mock(Player.class);
        UUID uuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getName()).thenReturn("SelfUser");
        org.bukkit.Server mockServer = mock(org.bukkit.Server.class);
        when(player.getServer()).thenReturn(mockServer);

        bukkitMock.when(() -> Bukkit.getPlayerExact("SelfUser")).thenReturn(player);

        boolean result = command.onCommand(player, mockCmd, "trust", new String[]{"grant", "SelfUser"});
        assertTrue(result);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(player).sendRichMessage(captor.capture());
        assertTrue(captor.getValue().contains("You cannot target yourself"));
    }

    @Test
    void testInfoSelf() {
        Player player = mock(Player.class);
        UUID uuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(uuid);

        boolean result = command.onCommand(player, mockCmd, "trust", new String[]{"info"});
        assertTrue(result);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(player).sendRichMessage(captor.capture());
        assertTrue(captor.getValue().contains("Trust List"));
    }

    @Test
    void testTabComplete() {
        CommandSender sender = mock(CommandSender.class);
        tabCompleteUtilMock.when(() -> TabCompleteUtil.filterStartsWith(Mockito.anyList(), Mockito.eq("")))
                .thenReturn(List.of("grant", "revoke", "block", "info"));

        List<String> list0 = command.onTabComplete(sender, mockCmd, "trust", new String[]{""});
        assertEquals(4, list0.size());

        tabCompleteUtilMock.when(() -> TabCompleteUtil.getOnlinePlayerNames("pl"))
                .thenReturn(new java.util.ArrayList<>(List.of("player1", "player2")));

        List<String> list2 = command.onTabComplete(sender, mockCmd, "trust", new String[]{"grant", "pl"});
        assertEquals(2, list2.size());

        List<String> list3 = command.onTabComplete(sender, mockCmd, "trust", new String[]{"grant", "p1", "extra"});
        assertEquals(Collections.emptyList(), list3);
    }
}
