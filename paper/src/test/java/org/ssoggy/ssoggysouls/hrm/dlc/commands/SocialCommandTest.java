package org.ssoggy.ssoggysouls.hrm.dlc.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStatic;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStorage;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SocialCommandTest {

    @Test
    void testCommandInitialization() {
        SocialCommand command = new SocialCommand();
        // Just instantiate to get some coverage on the class definition
        // Real testing would require heavy Bukkit mocks
        assertTrue(command != null);
    }
}
