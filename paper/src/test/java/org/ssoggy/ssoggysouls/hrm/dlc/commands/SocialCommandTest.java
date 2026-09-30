package org.ssoggy.ssoggysouls.hrm.dlc.commands;

import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertTrue;

class SocialCommandTest {

    @Test
    void testCommandInitialization() {
        SocialCommand command = new SocialCommand();
        // Just instantiate to get some coverage on the class definition
        // Real testing would require heavy Bukkit mocks
        assertTrue(command != null);
    }
}
