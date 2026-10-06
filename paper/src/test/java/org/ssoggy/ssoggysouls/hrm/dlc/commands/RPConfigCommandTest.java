package org.ssoggy.ssoggysouls.hrm.dlc.commands;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStatic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RPConfigCommandTest {

    private File tempDir;
    private JavaPlugin mockPlugin;
    private YamlConfiguration config;
    private RPConfigCommand cmd;
    private Command command;
    private CommandSender sender;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("rpconfigtest").toFile();
        mockPlugin = mock(JavaPlugin.class);
        Server mockServer = mock(Server.class);
        when(mockPlugin.getServer()).thenReturn(mockServer);
        when(mockPlugin.getDataFolder()).thenReturn(tempDir);
        when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("test"));
        config = new YamlConfiguration();
        when(mockPlugin.getConfig()).thenReturn(config);

        RPStatic.init(mockPlugin);

        RPStatic.BLOCK_TAGS.put("test-tag", new HashSet<>(Set.of(Material.STONE, Material.DIRT)));
        RPStatic.CONFIG_RULES.put("test-rule", true);
        RPStatic.CONFIG_TIMERS.put("test-timer", 100);

        cmd = new RPConfigCommand();
        command = mock(Command.class);
        sender = mock(CommandSender.class);
    }

    @AfterEach
    void tearDown() {
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
    void testNoArgsUsage() {
        boolean result = cmd.onCommand(sender, command, "revivalconfig", new String[0]);
        assertTrue(result);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sender).sendRichMessage(captor.capture());
        assertTrue(captor.getValue().contains("Please use"));
    }

    @Test
    void testInvalidOption() {
        boolean result = cmd.onCommand(sender, command, "revivalconfig", new String[]{"invalid"});
        assertTrue(result);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sender).sendRichMessage(captor.capture());
        assertTrue(captor.getValue().contains("Failure."));
    }

    @Test
    void testReload() {
        boolean result = cmd.onCommand(sender, command, "revivalconfig", new String[]{"reload"});
        assertTrue(result);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sender).sendRichMessage(captor.capture());
        assertTrue(captor.getValue().contains("reloading..."));
    }

    @Test
    void testTimerIncompleteArgs() {
        cmd.onCommand(sender, command, "revivalconfig", new String[]{"timer"});

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sender).sendRichMessage(captor.capture());
        assertTrue(captor.getValue().contains("Failure."));
    }

    @Test
    void testTimerDisplay() {
        cmd.onCommand(sender, command, "revivalconfig", new String[]{"timer", "test-timer"});

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sender).sendRichMessage(captor.capture());
        assertTrue(captor.getValue().contains("test-timer"));
        assertTrue(captor.getValue().contains("100s"));
    }

    @Test
    void testTimerInvalidKey() {
        cmd.onCommand(sender, command, "revivalconfig", new String[]{"timer", "non-existent", "50"});

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sender).sendRichMessage(captor.capture());
        assertTrue(captor.getValue().contains("Invalid timer: non-existent"));
    }

    @Test
    void testTimerNotANumber() {
        cmd.onCommand(sender, command, "revivalconfig", new String[]{"timer", "test-timer", "not-a-number"});

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sender).sendRichMessage(captor.capture());
        assertTrue(captor.getValue().contains("Timer value must be a number"));
    }

    @Test
    void testTimerSuccess() {
        cmd.onCommand(sender, command, "revivalconfig", new String[]{"timer", "test-timer", "200"});

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sender).sendRichMessage(captor.capture());
        assertTrue(captor.getValue().contains("Set test-timer to 200"));
        assertEquals(200, RPStatic.CONFIG_TIMERS.get("test-timer"));
    }

    @Test
    void testGameruleSuccess() {
        cmd.onCommand(sender, command, "revivalconfig", new String[]{"gamerule", "test-rule", "false"});

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sender).sendRichMessage(captor.capture());
        assertTrue(captor.getValue().contains("Set test-rule to false"));
        assertEquals(false, RPStatic.CONFIG_RULES.get("test-rule"));
    }

    @Test
    void testStructureList() {
        cmd.onCommand(sender, command, "revivalconfig", new String[]{"structure", "test-tag"});

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sender).sendRichMessage(captor.capture());
        assertTrue(captor.getValue().contains("Contents inside of"));
        assertTrue(captor.getValue().contains("STONE"));
    }

    @Test
    void testStructureInvalidTag() {
        cmd.onCommand(sender, command, "revivalconfig", new String[]{"structure", "unknown-tag"});

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sender).sendRichMessage(captor.capture());
        assertTrue(captor.getValue().contains("Failure."));
    }

    @Test
    void testStructureReset() {
        cmd.onCommand(sender, command, "revivalconfig", new String[]{"structure", "test-tag", "reset"});

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sender).sendRichMessage(captor.capture());
        assertTrue(captor.getValue().contains("Reset values in test-tag"));
    }

    @Test
    void testStructureAddMaterial() {
        cmd.onCommand(sender, command, "revivalconfig", new String[]{"structure", "test-tag", "add", "BEDROCK"});

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sender).sendRichMessage(captor.capture());
        assertTrue(captor.getValue().contains("Added BEDROCK to test-tag"));
        assertTrue(RPStatic.BLOCK_TAGS.get("test-tag").contains(Material.BEDROCK));
    }

    @Test
    void testStructureRemoveMaterial() {
        cmd.onCommand(sender, command, "revivalconfig", new String[]{"structure", "test-tag", "remove", "STONE"});

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sender).sendRichMessage(captor.capture());
        assertTrue(captor.getValue().contains("Removed STONE from test-tag"));
        assertFalse(RPStatic.BLOCK_TAGS.get("test-tag").contains(Material.STONE));
    }

    @Test
    void testStructureInvalidMaterial() {
        cmd.onCommand(sender, command, "revivalconfig", new String[]{"structure", "test-tag", "add", "INVALID_BLOCK"});

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sender).sendRichMessage(captor.capture());
        assertTrue(captor.getValue().contains("Invalid BlockMaterial entered"));
    }

    @Test
    void testStructureRemoveAll() {
        cmd.onCommand(sender, command, "revivalconfig", new String[]{"structure", "test-tag", "remove", "ALL"});

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sender).sendRichMessage(captor.capture());
        assertTrue(captor.getValue().contains("Removed everything from test-tag"));
        assertTrue(RPStatic.BLOCK_TAGS.get("test-tag").isEmpty());
    }

    @Test
    void testStructureInvalidAction() {
        cmd.onCommand(sender, command, "revivalconfig", new String[]{"structure", "test-tag", "invalid_action", "STONE"});

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sender).sendRichMessage(captor.capture());
        assertTrue(captor.getValue().contains("Use add, remove, or reset."));
    }

    @Test
    void testGameruleIncompleteArgs() {
        cmd.onCommand(sender, command, "revivalconfig", new String[]{"gamerule"});

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sender).sendRichMessage(captor.capture());
        assertTrue(captor.getValue().contains("Failure."));
    }

    @Test
    void testGameruleDisplay() {
        cmd.onCommand(sender, command, "revivalconfig", new String[]{"gamerule", "test-rule"});

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sender).sendRichMessage(captor.capture());
        assertTrue(captor.getValue().contains("test-rule"));
        assertTrue(captor.getValue().contains("true"));
    }

    @Test
    void testGameruleInvalidRule() {
        cmd.onCommand(sender, command, "revivalconfig", new String[]{"gamerule", "unknown-rule"});

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sender).sendRichMessage(captor.capture());
        assertTrue(captor.getValue().contains("Failure."));
    }

    @Test
    void testTabComplete() {
        // Tab complete options
        List<String> opt1 = cmd.onTabComplete(sender, command, "revivalconfig", new String[]{""});
        assertTrue(opt1.contains("structure"));
        assertTrue(opt1.contains("gamerule"));

        // Tab complete keys
        List<String> opt2 = cmd.onTabComplete(sender, command, "revivalconfig", new String[]{"structure", ""});
        assertTrue(opt2.contains("test-tag"));

        // Tab complete actions
        List<String> opt3 = cmd.onTabComplete(sender, command, "revivalconfig", new String[]{"structure", "test-tag", ""});
        assertTrue(opt3.contains("add"));
        assertTrue(opt3.contains("remove"));

        // Tab complete remove material
        List<String> opt4 = cmd.onTabComplete(sender, command, "revivalconfig", new String[]{"structure", "test-tag", "remove", "STO"});
        assertTrue(opt4.contains("STONE"));
    }
}
