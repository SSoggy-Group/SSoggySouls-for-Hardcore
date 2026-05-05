package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeUtilTest {

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
        "5430000, 1h 30m",
        "3630000, 1h"
    })
    void testFormatTime(long millis, String expected) {
        assertEquals(expected, TimeUtil.formatTime(millis));
    }
}
