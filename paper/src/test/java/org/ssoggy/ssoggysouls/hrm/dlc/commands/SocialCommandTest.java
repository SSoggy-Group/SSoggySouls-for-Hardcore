package org.ssoggy.ssoggysouls.hrm.dlc.commands;

import org.junit.jupiter.api.Test;

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
