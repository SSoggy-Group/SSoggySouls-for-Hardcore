package org.ssoggy.ssoggysouls.hrm.dlc.shared;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ssoggy.ssoggysouls.PluginContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DlcTrustServiceTest {

    private File tempFolder;
    private UUID playerUuid;
    private UUID targetUuid;
    private String playerName = "Player";
    private String targetName = "Target";

    @BeforeEach
    void setup() throws IOException {
        tempFolder = Files.createTempDirectory("dlcservices").toFile();
        PluginContext context = mock(PluginContext.class);
        when(context.getLogger()).thenReturn(mock(Logger.class));
        when(context.getDataFolder()).thenReturn(tempFolder);
        DlcServices.init(context);

        playerUuid = UUID.randomUUID();
        targetUuid = UUID.randomUUID();
    }

    @AfterEach
    void tearDown() {
        for (File f : new File(tempFolder, "revivalplus").listFiles()) {
            f.delete();
        }
        new File(tempFolder, "revivalplus").delete();
        tempFolder.delete();
    }

    @Test
    void testBlock() {
        DlcTrustService.TrustResult result = DlcTrustService.execute(playerUuid, playerName, targetUuid, targetName, DlcTrustAction.BLOCK);
        assertEquals(DlcCommandResult.Status.TRUE, result.result().status());
        assertEquals("You have blocked Target", result.result().message());

        // Second block should fail
        result = DlcTrustService.execute(playerUuid, playerName, targetUuid, targetName, DlcTrustAction.BLOCK);
        assertEquals(DlcCommandResult.Status.INFO, result.result().status());
        assertEquals("You already blocked Target", result.result().message());
    }

    @Test
    void testRevoke() {
        DlcTrustService.TrustResult result = DlcTrustService.execute(playerUuid, playerName, targetUuid, targetName, DlcTrustAction.REVOKE);
        assertEquals(DlcCommandResult.Status.INFO, result.result().status());
        assertEquals("You have no relations with Target", result.result().message());

        // Block then revoke
        DlcTrustService.execute(playerUuid, playerName, targetUuid, targetName, DlcTrustAction.BLOCK);
        result = DlcTrustService.execute(playerUuid, playerName, targetUuid, targetName, DlcTrustAction.REVOKE);
        assertEquals(DlcCommandResult.Status.TRUE, result.result().status());
        assertEquals("You no longer trust Target", result.result().message());
    }

    @Test
    void testGrant() {
        DlcTrustService.TrustResult result = DlcTrustService.execute(playerUuid, playerName, targetUuid, targetName, DlcTrustAction.GRANT);
        assertEquals(DlcCommandResult.Status.TRUE, result.result().status());
        assertEquals("You have now entrusted Target", result.result().message());

        // Second grant should fail
        result = DlcTrustService.execute(playerUuid, playerName, targetUuid, targetName, DlcTrustAction.GRANT);
        assertEquals(DlcCommandResult.Status.INFO, result.result().status());
        assertEquals("You have already entrusted Target", result.result().message());
    }

    @Test
    void testMutualGrant() {
        DlcTrustService.execute(playerUuid, playerName, targetUuid, targetName, DlcTrustAction.GRANT);
        DlcTrustService.TrustResult result = DlcTrustService.execute(targetUuid, targetName, playerUuid, playerName, DlcTrustAction.GRANT);
        assertEquals(DlcCommandResult.Status.TRUE, result.result().status());
        assertEquals("You are now friends with Player", result.result().message());
        assertEquals("You are now friends with Target", result.targetMessage());
    }
}
