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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DlcTrustServiceTest {

    private File tempFolder;
    private UUID playerUuid;
    private UUID targetUuid;
    private final String playerName = "Player";
    private final String targetName = "Target";

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
        File rplus = new File(tempFolder, "revivalplus");
        if (rplus.exists()) {
            File[] files = rplus.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
            rplus.delete();
        }
        tempFolder.delete();
    }

    @Test
    void testBlock() {
        DlcTrustService.TrustResult result = DlcTrustService.execute(playerUuid, playerName, targetUuid, targetName, DlcTrustAction.BLOCK);
        assertEquals(DlcCommandResult.Status.TRUE, result.result().status());
        assertEquals("You have blocked Target", result.result().message());

        // Second block should return info
        result = DlcTrustService.execute(playerUuid, playerName, targetUuid, targetName, DlcTrustAction.BLOCK);
        assertEquals(DlcCommandResult.Status.INFO, result.result().status());
        assertEquals("You already blocked Target", result.result().message());
    }

    @Test
    void testBlockBreaksTrustworthyRelationship() {
        // Player and Target mutual grant (friends)
        DlcTrustService.execute(playerUuid, playerName, targetUuid, targetName, DlcTrustAction.GRANT);
        DlcTrustService.execute(targetUuid, targetName, playerUuid, playerName, DlcTrustAction.GRANT);

        // Player blocks Target
        DlcTrustService.execute(playerUuid, playerName, targetUuid, targetName, DlcTrustAction.BLOCK);

        DlcSocial targetSocial = new DlcSocial(targetUuid);
        assertEquals(DlcRelation.UNTRUSTED, targetSocial.getRelationTo(playerUuid));
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
    void testRevokeFriendsDemotesTargetToTrusted() {
        // Form mutual friendship
        DlcTrustService.execute(playerUuid, playerName, targetUuid, targetName, DlcTrustAction.GRANT);
        DlcTrustService.execute(targetUuid, targetName, playerUuid, playerName, DlcTrustAction.GRANT);

        // Player revokes friendship
        DlcTrustService.TrustResult result = DlcTrustService.execute(playerUuid, playerName, targetUuid, targetName, DlcTrustAction.REVOKE);
        assertEquals(DlcCommandResult.Status.TRUE, result.result().status());
        assertEquals("You no longer trust Target", result.result().message());

        assertEquals(DlcRelation.UNTRUSTED, new DlcSocial(playerUuid).getRelationTo(targetUuid));
        assertEquals(DlcRelation.TRUSTED, new DlcSocial(targetUuid).getRelationTo(playerUuid));
    }

    @Test
    void testGrant() {
        DlcTrustService.TrustResult result = DlcTrustService.execute(playerUuid, playerName, targetUuid, targetName, DlcTrustAction.GRANT);
        assertEquals(DlcCommandResult.Status.TRUE, result.result().status());
        assertEquals("You have now entrusted Target", result.result().message());

        // Second grant should return info
        result = DlcTrustService.execute(playerUuid, playerName, targetUuid, targetName, DlcTrustAction.GRANT);
        assertEquals(DlcCommandResult.Status.INFO, result.result().status());
        assertEquals("You have already entrusted Target", result.result().message());
    }

    @Test
    void testGrantWhenTargetHasBlockedPlayer() {
        // Target blocks Player
        DlcTrustService.execute(targetUuid, targetName, playerUuid, playerName, DlcTrustAction.BLOCK);

        // Player attempts to grant Target
        DlcTrustService.TrustResult result = DlcTrustService.execute(playerUuid, playerName, targetUuid, targetName, DlcTrustAction.GRANT);
        assertEquals(DlcCommandResult.Status.FALSE, result.result().status());
        assertEquals("Player has you blocked.", result.result().message());
    }

    @Test
    void testMutualGrant() {
        DlcTrustService.execute(playerUuid, playerName, targetUuid, targetName, DlcTrustAction.GRANT);
        DlcTrustService.TrustResult result = DlcTrustService.execute(targetUuid, targetName, playerUuid, playerName, DlcTrustAction.GRANT);
        assertEquals(DlcCommandResult.Status.TRUE, result.result().status());
        assertEquals("You are now friends with Player", result.result().message());
        assertEquals("You are now friends with Target", result.targetMessage());
    }

    @Test
    void testSelfTarget() {
        DlcTrustService.TrustResult result = DlcTrustService.execute(playerUuid, playerName, playerUuid, playerName, DlcTrustAction.GRANT);
        assertEquals(DlcCommandResult.Status.FALSE, result.result().status());
        assertEquals("You cannot target yourself", result.result().message());
    }

    @Test
    void testMissingTarget() {
        DlcTrustService.TrustResult result1 = DlcTrustService.execute(playerUuid, playerName, null, null, DlcTrustAction.GRANT);
        assertEquals(DlcCommandResult.Status.FALSE, result1.result().status());
        assertEquals("Please use /trust <action> [player]", result1.result().message());

        DlcTrustService.TrustResult result2 = DlcTrustService.execute(playerUuid, playerName, targetUuid, "   ", DlcTrustAction.GRANT);
        assertEquals(DlcCommandResult.Status.FALSE, result2.result().status());
        assertEquals("Please use /trust <action> [player]", result2.result().message());
    }

    @Test
    void testInfoActionEmptyList() {
        DlcTrustService.TrustResult result = DlcTrustService.execute(playerUuid, playerName, null, null, DlcTrustAction.INFO);
        assertEquals(DlcCommandResult.Status.INFO, result.result().status());
        assertEquals("Your trust list is empty.", result.result().message());
        assertNull(result.targetMessage());
    }

    @Test
    void testInfoActionWithRelations() {
        DlcTrustService.execute(playerUuid, playerName, targetUuid, targetName, DlcTrustAction.GRANT);

        DlcTrustService.TrustResult result = DlcTrustService.execute(playerUuid, playerName, null, null, DlcTrustAction.INFO);
        assertEquals(DlcCommandResult.Status.RAW, result.result().status());
        assertTrue(result.result().message().contains("--- Trust List ---"));
        assertTrue(result.result().message().contains("Target: TRUSTED"));
    }
}
