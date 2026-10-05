package org.ssoggy.ssoggysouls.model;

import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerDataTest {

    private static final UUID TEST_UUID = UUID.fromString("12345678-1234-1234-1234-123456789abc");
    private static final String TEST_NAME = "TestPlayer";

    @Test
    void testIsInGracePeriodExplicitGrace() {
        long now = System.currentTimeMillis();
        // Active explicit grace until 1 hour from now
        PlayerData activeData = new PlayerData(TEST_UUID, TEST_NAME, 3, false, now - 1000, 0, 0, now + 3600_000L);
        assertTrue(activeData.isInGracePeriod(0));
        assertTrue(activeData.isInGracePeriod(1800_000L));

        // Expired explicit grace (graceUntil in the past)
        PlayerData expiredData = new PlayerData(TEST_UUID, TEST_NAME, 3, false, now - 5000, 0, 0, now - 1000L);
        assertFalse(expiredData.isInGracePeriod(0));
        assertFalse(expiredData.isInGracePeriod(3600_000L));
    }

    @Test
    void testIsInGracePeriodLegacyFallback() {
        long now = System.currentTimeMillis();

        // Legacy fallback (graceUntil = 0): gracePeriodMillis <= 0 returns false
        PlayerData legacyData = new PlayerData(TEST_UUID, TEST_NAME, 3, false, now - 1000, 0, 0, 0L);
        assertFalse(legacyData.isInGracePeriod(0));
        assertFalse(legacyData.isInGracePeriod(-100));

        // Legacy fallback: joined 30 seconds ago, gracePeriodMillis = 60 seconds -> true
        PlayerData activeLegacy = new PlayerData(TEST_UUID, TEST_NAME, 3, false, now - 30_000L, 0, 0, 0L);
        assertTrue(activeLegacy.isInGracePeriod(60_000L));

        // Legacy fallback: joined 2 hours ago, gracePeriodMillis = 1 hour -> false
        PlayerData expiredLegacy = new PlayerData(TEST_UUID, TEST_NAME, 3, false, now - 7200_000L, 0, 0, 0L);
        assertFalse(expiredLegacy.isInGracePeriod(3600_000L));

        // Legacy fallback: firstJoin in the future (elapsed < 0) -> false
        PlayerData futureLegacy = new PlayerData(TEST_UUID, TEST_NAME, 3, false, now + 10_000L, 0, 0, 0L);
        assertFalse(futureLegacy.isInGracePeriod(3600_000L));
    }

    @Test
    void testGetGraceTimeRemainingExplicitGrace() {
        long now = System.currentTimeMillis();

        // Explicit grace remaining > 1 hour (e.g., 2h 30m remaining with 10s buffer for execution drift)
        long graceUntilHours = now + (2 * 3600_000L + 30 * 60_000L + 10_000L);
        PlayerData dataHours = new PlayerData(TEST_UUID, TEST_NAME, 3, false, now - 1000, 0, 0, graceUntilHours);
        assertEquals("2h 30m", dataHours.getGraceTimeRemaining(0));

        // Explicit grace remaining < 1 hour (e.g., 15m remaining with 10s buffer)
        long graceUntilMinutes = now + (15 * 60_000L + 10_000L);
        PlayerData dataMinutes = new PlayerData(TEST_UUID, TEST_NAME, 3, false, now - 1000, 0, 0, graceUntilMinutes);
        assertEquals("15m", dataMinutes.getGraceTimeRemaining(0));

        // Explicit grace expired
        long graceUntilExpired = now - 1000L;
        PlayerData dataExpired = new PlayerData(TEST_UUID, TEST_NAME, 3, false, now - 5000, 0, 0, graceUntilExpired);
        assertEquals("0m", dataExpired.getGraceTimeRemaining(0));
    }

    @Test
    void testGetGraceTimeRemainingLegacyFallback() {
        long now = System.currentTimeMillis();

        // Legacy fallback: firstJoin = now - 30 mins, config gracePeriod = 2h 15m (+ 10s buffer) -> remaining ~1h 45m
        long firstJoin = now - (30 * 60_000L);
        long gracePeriod = 2 * 3600_000L + 15 * 60_000L + 10_000L;
        PlayerData dataHours = new PlayerData(TEST_UUID, TEST_NAME, 3, false, firstJoin, 0, 0, 0L);
        assertEquals("1h 45m", dataHours.getGraceTimeRemaining(gracePeriod));

        // Legacy fallback: remaining < 1 hour (e.g. 20m remaining with 10s buffer)
        long firstJoinShort = now - (10 * 60_000L);
        long gracePeriodShort = 30 * 60_000L + 10_000L;
        PlayerData dataMinutes = new PlayerData(TEST_UUID, TEST_NAME, 3, false, firstJoinShort, 0, 0, 0L);
        assertEquals("20m", dataMinutes.getGraceTimeRemaining(gracePeriodShort));

        // Legacy fallback expired
        long firstJoinOld = now - 7200_000L;
        PlayerData dataExpired = new PlayerData(TEST_UUID, TEST_NAME, 3, false, firstJoinOld, 0, 0, 0L);
        assertEquals("0m", dataExpired.getGraceTimeRemaining(3600_000L));
    }

    @Test
    void testCreateNew() {
        long before = System.currentTimeMillis();
        PlayerData playerNoGrace = PlayerData.createNew(TEST_UUID, TEST_NAME, 5);
        long after = System.currentTimeMillis();

        assertEquals(TEST_UUID, playerNoGrace.getUuid());
        assertEquals(TEST_NAME, playerNoGrace.getUsername());
        assertEquals(5, playerNoGrace.getLives());
        assertFalse(playerNoGrace.isDead());
        assertEquals(0L, playerNoGrace.getLastDeath());
        assertEquals(0L, playerNoGrace.getLastSeen());
        assertEquals(0L, playerNoGrace.getGraceUntil());
        assertTrue(playerNoGrace.getFirstJoin() >= before && playerNoGrace.getFirstJoin() <= after);

        // With positive grace period millis
        PlayerData playerWithGrace = PlayerData.createNew(TEST_UUID, TEST_NAME, 5, 3600_000L);
        assertTrue(playerWithGrace.getGraceUntil() >= before + 3600_000L);

        // With zero grace period millis
        PlayerData playerZeroGrace = PlayerData.createNew(TEST_UUID, TEST_NAME, 5, 0L);
        assertEquals(0L, playerZeroGrace.getGraceUntil());
    }

    @Test
    void testDecrementLifeAndRevive() {
        PlayerData player = new PlayerData(TEST_UUID, TEST_NAME, 2, false, System.currentTimeMillis(), 0, 0, 0);

        // Decrement from 2 to 1
        int livesLeft = player.decrementLife();
        assertEquals(1, livesLeft);
        assertEquals(1, player.getLives());
        assertFalse(player.isDead());

        // Decrement from 1 to 0 -> should set isDead to true and set lastDeath
        long beforeDeath = System.currentTimeMillis();
        livesLeft = player.decrementLife();
        long afterDeath = System.currentTimeMillis();

        assertEquals(0, livesLeft);
        assertEquals(0, player.getLives());
        assertTrue(player.isDead());
        assertTrue(player.getLastDeath() >= beforeDeath && player.getLastDeath() <= afterDeath);

        // Decrementing when already 0 -> remains 0 and dead
        livesLeft = player.decrementLife();
        assertEquals(0, livesLeft);
        assertTrue(player.isDead());

        // Revive
        player.revive(3);
        assertEquals(3, player.getLives());
        assertFalse(player.isDead());
    }

    @Test
    void testGettersAndSetters() {
        PlayerData player = new PlayerData(TEST_UUID, TEST_NAME, 3, false, 100L, 200L, 300L, 400L);

        player.setUsername("NewName");
        assertEquals("NewName", player.getUsername());

        player.setLives(10);
        assertEquals(10, player.getLives());

        player.setDead(true);
        assertTrue(player.isDead());

        player.setFirstJoin(1000L);
        assertEquals(1000L, player.getFirstJoin());

        player.setLastDeath(2000L);
        assertEquals(2000L, player.getLastDeath());

        player.setLastSeen(3000L);
        assertEquals(3000L, player.getLastSeen());

        player.setGraceUntil(4000L);
        assertEquals(4000L, player.getGraceUntil());
    }

    @Test
    void testToString() {
        PlayerData player = new PlayerData(TEST_UUID, TEST_NAME, 3, false, 100L, 0L, 0L, 0L);
        String str = player.toString();

        assertTrue(str.contains(TEST_UUID.toString()));
        assertTrue(str.contains(TEST_NAME));
        assertTrue(str.contains("lives=3"));
        assertTrue(str.contains("dead=false"));
    }
}
