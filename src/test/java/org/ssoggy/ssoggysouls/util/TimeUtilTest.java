package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TimeUtilTest {

    @Test
    public void testParseTimeToMillis_NullOrEmpty() {
        assertEquals(-1, TimeUtil.parseTimeToMillis(null));
        assertEquals(-1, TimeUtil.parseTimeToMillis(""));
        assertEquals(-1, TimeUtil.parseTimeToMillis("   "));
    }

    @Test
    public void testParseTimeToMillis_PlainInteger() {
        assertEquals(3600_000L, TimeUtil.parseTimeToMillis("1"));
        assertEquals(7200_000L, TimeUtil.parseTimeToMillis("2"));
        assertEquals(0L, TimeUtil.parseTimeToMillis("0"));
    }

    @Test
    public void testParseTimeToMillis_TimeComponents() {
        assertEquals(3600_000L, TimeUtil.parseTimeToMillis("1h"));
        assertEquals(7200_000L, TimeUtil.parseTimeToMillis("2h"));
        assertEquals(60_000L, TimeUtil.parseTimeToMillis("1m"));
        assertEquals(120_000L, TimeUtil.parseTimeToMillis("2m"));
        assertEquals(1000L, TimeUtil.parseTimeToMillis("1s"));
        assertEquals(2000L, TimeUtil.parseTimeToMillis("2s"));
    }

    @Test
    public void testParseTimeToMillis_CombinedComponents() {
        assertEquals(3661_000L, TimeUtil.parseTimeToMillis("1h1m1s"));
        assertEquals(7322_000L, TimeUtil.parseTimeToMillis("2h2m2s"));
        assertEquals(3601_000L, TimeUtil.parseTimeToMillis("1h1s"));
        assertEquals(61_000L, TimeUtil.parseTimeToMillis("1m1s"));
    }

    @Test
    public void testParseTimeToMillis_InvalidInput() {
        assertEquals(-1, TimeUtil.parseTimeToMillis("invalid"));
        assertEquals(-1, TimeUtil.parseTimeToMillis("abc"));
        // if parts are valid it might parse them, let's see: TimeUtil doesn't strictly reject 'invalid1h' because matcher.find() looks for anywhere in string.
        assertEquals(-1L, TimeUtil.parseTimeToMillis("invalid1h"));
    }

}
