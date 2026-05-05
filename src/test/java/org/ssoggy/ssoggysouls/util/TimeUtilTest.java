package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeUtilTest {

    @Test
    void testFormatTimeNegativeAndZero() {
        assertEquals("0s", TimeUtil.formatTime(-1000L));
        assertEquals("0s", TimeUtil.formatTime(0L));
    }

    @Test
    void testFormatTimeUnderOneSecond() {
        assertEquals("0s", TimeUtil.formatTime(500L));
    }

    @Test
    void testFormatTimeSecondsOnly() {
        assertEquals("45s", TimeUtil.formatTime(45_000L));
    }

    @Test
    void testFormatTimeMinutesOnly() {
        assertEquals("2m", TimeUtil.formatTime(120_000L));
    }

    @Test
    void testFormatTimeMinutesAndSeconds() {
        assertEquals("2m 30s", TimeUtil.formatTime(150_000L));
    }

    @Test
    void testFormatTimeHoursOnly() {
        assertEquals("1h", TimeUtil.formatTime(3600_000L));
    }

    @Test
    void testFormatTimeHoursAndMinutes() {
        assertEquals("1h 30m", TimeUtil.formatTime(5400_000L));
    }

    @Test
    void testFormatTimeHoursMinutesAndSeconds() {
        // Seconds should be omitted when hours > 0
        assertEquals("1h 30m", TimeUtil.formatTime(5430_000L));
    }

    @Test
    void testFormatTimeHoursAndSeconds() {
        // Seconds should be omitted when hours > 0 even if minutes == 0
        assertEquals("1h", TimeUtil.formatTime(3630_000L));
    }
}
