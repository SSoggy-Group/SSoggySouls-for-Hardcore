package org.ssoggy.ssoggysouls.hrm.dlc.commands;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.ssoggy.ssoggysouls.hrm.dlc.enums.SOCIALENUM;
import org.ssoggy.ssoggysouls.hrm.dlc.util.Pair;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPSocial;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStatic;
import org.ssoggy.ssoggysouls.util.MessageUtil;
import org.ssoggy.ssoggysouls.util.TabCompleteUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ObituariesCommandTest {

    private File tempDir;
    private JavaPlugin mockPlugin;
    private YamlConfiguration config;
    private ObituariesCommand cmd;
    private Command command;
    private MockedStatic<TabCompleteUtil> tabCompleteUtilMock;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("obituariestest").toFile();
        mockPlugin = mock(JavaPlugin.class);
        Server mockServer = mock(Server.class);
        when(mockPlugin.getServer()).thenReturn(mockServer);
        when(mockPlugin.getDataFolder()).thenReturn(tempDir);
        when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("test"));
        config = new YamlConfiguration();
        when(mockPlugin.getConfig()).thenReturn(config);

        RPStatic.init(mockPlugin);

        cmd = new ObituariesCommand();
        command = mock(Command.class);
        tabCompleteUtilMock = Mockito.mockStatic(TabCompleteUtil.class);
    }

    @AfterEach
    void tearDown() {
        if (tabCompleteUtilMock != null) tabCompleteUtilMock.close();
        if (RPStatic.DEAD_STORAGE != null) RPStatic.DEAD_STORAGE.shutdown();
        if (RPStatic.STATS_STORAGE != null) RPStatic.STATS_STORAGE.shutdown();
        if (RPStatic.SOCIAL_STORAGE != null) RPStatic.SOCIAL_STORAGE.shutdown();
        if (RPStatic.USERNAME_CACHE != null) RPStatic.USERNAME_CACHE.shutdown();

        File[] files = tempDir.listFiles();
        if (files != null) {
            for (File f : files) f.delete();
        }
        tempDir.delete();
    }

    @Test
    void testSenderNotPlayer() {
        CommandSender sender = mock(CommandSender.class);
        boolean result = cmd.onCommand(sender, command, "deathlist", new String[0]);
        assertTrue(result);
        verify(sender).sendMessage(MessageUtil.get("command-only-players"));
    }

    @Test
    void testNoPublicDeathsCurrently() {
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());

        boolean result = cmd.onCommand(player, command, "deathlist", new String[0]);
        assertTrue(result);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(player).sendRichMessage(captor.capture());
        assertTrue(captor.getValue().contains("There are no public deaths currently."));
    }

    @Test
    void testPublicDeathsWithTrustedRelationship() {
        config.set("hrm.trusted-obituary-after", 60L);

        Player player = mock(Player.class);
        UUID viewerUuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(viewerUuid);

        UUID deadUuid = UUID.randomUUID();
        World world = mock(World.class);
        when(world.getName()).thenReturn("world_nether");
        Location deathLoc = new Location(world, 100, 65, -50);

        // 90 minutes ago (exceeds default 60 min trusted delay, but below public delay 3600 min)
        Instant deathTime = Instant.now().minusSeconds(90 * 60);
        RPStatic.DEAD_LOCATIONS.put(deadUuid, Pair.of(deathLoc, deathTime));

        // Set relation as TRUSTED
        new RPSocial(deadUuid).setRelationTo(viewerUuid, SOCIALENUM.TRUSTED);
        RPStatic.USERNAME_CACHE.setValue("usernamecache", deadUuid.toString(), "DeadPlayer1");

        boolean result = cmd.onCommand(player, command, "deathlist", new String[0]);
        assertTrue(result);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(player).sendRichMessage(captor.capture());
        String msg = captor.getValue();
        assertTrue(msg.contains("DeadPlayer1"));
        assertTrue(msg.contains("world_nether"));
        assertTrue(msg.contains("X100 Y65 Z-50"));
    }

    @Test
    void testPublicDeathsTooRecentForFriends() {
        Player player = mock(Player.class);
        UUID viewerUuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(viewerUuid);

        UUID deadUuid = UUID.randomUUID();
        World world = mock(World.class);
        when(world.getName()).thenReturn("world");
        Location deathLoc = new Location(world, 0, 70, 0);

        // Only 10 minutes ago - neither trusted (60m) nor friends (600m) nor public (3600m)
        Instant deathTime = Instant.now().minusSeconds(10 * 60);
        RPStatic.DEAD_LOCATIONS.put(deadUuid, Pair.of(deathLoc, deathTime));

        new RPSocial(deadUuid).setRelationTo(viewerUuid, SOCIALENUM.FRIENDS);

        boolean result = cmd.onCommand(player, command, "deathlist", new String[0]);
        assertTrue(result);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(player).sendRichMessage(captor.capture());
        assertTrue(captor.getValue().contains("There are no public deaths currently."));
    }

    @Test
    void testTabComplete() {
        tabCompleteUtilMock.when(() -> TabCompleteUtil.getOnlinePlayerNames("pl"))
                .thenReturn(List.of("player1", "player2"));

        List<String> list1 = cmd.onTabComplete(mock(CommandSender.class), command, "deathlist", new String[]{"pl"});
        assertEquals(List.of("player1", "player2"), list1);

        List<String> list2 = cmd.onTabComplete(mock(CommandSender.class), command, "deathlist", new String[]{"p1", "extra"});
        assertEquals(Collections.emptyList(), list2);
    }
}
