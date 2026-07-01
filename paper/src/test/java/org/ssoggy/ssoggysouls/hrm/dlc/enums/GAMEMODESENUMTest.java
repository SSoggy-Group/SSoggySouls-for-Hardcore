package org.ssoggy.ssoggysouls.hrm.dlc.enums;

import org.junit.jupiter.api.Test;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStatic;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GAMEMODESENUMTest {

    @Test
    void testGhostPlayersInitialized() throws Exception {
        File dir = new File("build/tmp/test");
        dir.mkdirs();
        File f = new File(dir, "ghostmodeplayers.yml");
        Files.writeString(f.toPath(), "ghostmodeplayers:\n  \"00000000-0000-0000-0000-000000000000\": \"Player1\"\n  \"invalid-uuid\": \"Player2\"\n");

        JavaPlugin mockPlugin = mock(JavaPlugin.class);
        when(mockPlugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("test"));
        when(mockPlugin.getDataFolder()).thenReturn(dir);
        RPStatic.CLIENT = mockPlugin;

        // Trigger class loading to execute static block
        GAMEMODESENUM gm = GAMEMODESENUM.SURVIVAL;
        assertNotNull(gm);

        assertEquals(4, GAMEMODESENUM.GHOSTMODE.getGameModeID());
        assertEquals(org.bukkit.GameMode.ADVENTURE, GAMEMODESENUM.GHOSTMODE.getGameModeFallback());
    }
}
