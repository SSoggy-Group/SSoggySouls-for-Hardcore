package org.ssoggy.ssoggysouls.hrm.dlc.enums;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import java.util.logging.Logger;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStatic;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import org.bukkit.plugin.java.JavaPlugin;

class GAMEMODESENUMTest {

    @BeforeAll
    static void setUpAll() {
        RPStatic.LOGGER = mock(Logger.class);
        RPStatic.CLIENT = mock(JavaPlugin.class);
        when(RPStatic.CLIENT.getLogger()).thenReturn(RPStatic.LOGGER);
        when(RPStatic.CLIENT.getDataFolder()).thenReturn(new java.io.File("."));

        // Ensure static initialization of GAMEMODESENUM triggers after mocks are set up
        try {
            Class.forName("org.ssoggy.ssoggysouls.hrm.dlc.enums.GAMEMODESENUM");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testAddGhostToCacheSafelyValid() {
        UUID testUuid = UUID.randomUUID();
        GAMEMODESENUM.addGhostToCacheSafely(testUuid.toString());
        assertTrue(GAMEMODESENUM.getGhostCache().contains(testUuid));
    }

    @Test
    void testAddGhostToCacheSafelyInvalid() {
        String invalidUuid = "not-a-uuid";
        GAMEMODESENUM.addGhostToCacheSafely(invalidUuid);
        verify(RPStatic.LOGGER).warning("Invalid UUID format in ghost cache config: " + invalidUuid);
    }
}
