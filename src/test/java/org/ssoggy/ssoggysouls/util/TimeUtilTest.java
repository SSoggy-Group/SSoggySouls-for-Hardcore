package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class TimeUtilTest {

    @Test
    public void testParseTimeToMillis_ValidComponents() {
        assertEquals(3600_000L + 1800_000L, TimeUtil.parseTimeToMillis("1h30m"));
        assertEquals(60_000L * 90, TimeUtil.parseTimeToMillis("90m"));
        assertEquals(1000L * 45, TimeUtil.parseTimeToMillis("45s"));
        assertEquals(3600_000L * 2 + 60_000L * 15 + 1000L * 30, TimeUtil.parseTimeToMillis("2h15m30s"));
        assertEquals(3600_000L * 2 + 60_000L * 15 + 1000L * 30, TimeUtil.parseTimeToMillis("  2H15M30S  "));
    }

    @Test
    public void testParseTimeToMillis_ValidInteger() {
        assertEquals(3600_000L * 5, TimeUtil.parseTimeToMillis("5"));
        assertEquals(3600_000L * 24, TimeUtil.parseTimeToMillis("24"));
    }

    @Test
    public void testParseTimeToMillis_InvalidStrings() {
        assertEquals(-1L, TimeUtil.parseTimeToMillis(""));
        assertEquals(-1L, TimeUtil.parseTimeToMillis("   "));
        assertEquals(-1L, TimeUtil.parseTimeToMillis(null));
        assertEquals(-1L, TimeUtil.parseTimeToMillis("invalid"));
        assertEquals(-1L, TimeUtil.parseTimeToMillis("abc"));
        assertEquals(-1L, TimeUtil.parseTimeToMillis("1x2y"));
    }

    @Test
    public void testParseTimeToMillis_EdgeCases() {
        // overflow: values exceeding Integer.MAX_VALUE are handled via Long.parseLong
        assertEquals(3000000000L * 3600_000L, TimeUtil.parseTimeToMillis("3000000000h"));
        // negative plain integer is invalid
        assertEquals(-1L, TimeUtil.parseTimeToMillis("-5"));
        // negative component: regex won't match '-', so no components found
        assertEquals(-1L, TimeUtil.parseTimeToMillis("-1h"));
        // zero hours is a valid zero-length duration
        assertEquals(0L, TimeUtil.parseTimeToMillis("0"));
        // partial match: valid components are summed, unrecognised tokens are rejected
        assertEquals(-1L, TimeUtil.parseTimeToMillis("1h2x3m"));
        // spaces between components are rejected by strict regex "^(\\d+[hms])+$"
        assertEquals(-1L, TimeUtil.parseTimeToMillis("1h 30m"));
    }

    @ParameterizedTest(name = "formatTime({0}ms) -> {1}")
    @CsvSource({
        "-1000,   0s",
        "0,       0s",
        "500,     0s",
        "45000,   45s",
        "120000,  2m",
        "150000,  2m 30s",
        "3600000, 1h",
        "5400000, 1h 30m",
        "5430000, 1h 30m 30s",
        "3630000, 1h 30s"
    })
    void testFormatTime(long millis, String expected) {
        assertEquals(expected, TimeUtil.formatTime(millis));
    }
}
