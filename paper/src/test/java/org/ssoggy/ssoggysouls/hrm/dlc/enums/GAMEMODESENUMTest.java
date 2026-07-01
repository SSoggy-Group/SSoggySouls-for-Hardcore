package org.ssoggy.ssoggysouls.hrm.dlc.enums;

import org.junit.jupiter.api.Test;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStatic;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GAMEMODESENUMTest {

    @Test
    void testGhostPlayersInitialized() {
        JavaPlugin mockPlugin = mock(JavaPlugin.class);
        when(mockPlugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("test"));
        when(mockPlugin.getDataFolder()).thenReturn(new File("build/tmp/test"));
        RPStatic.CLIENT = mockPlugin;

        // Trigger class loading to execute static block
        GAMEMODESENUM gm = GAMEMODESENUM.SURVIVAL;
        assertNotNull(gm);

        // Let's actually test some functionality so coverage hits the lines
        // We know GHOSTMODE has ID 4
        assertEquals(4, GAMEMODESENUM.GHOSTMODE.getGameModeID());
        assertEquals(org.bukkit.GameMode.ADVENTURE, GAMEMODESENUM.GHOSTMODE.getGameModeFallback());
    }
}
