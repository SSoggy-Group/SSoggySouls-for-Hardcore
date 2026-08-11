package org.ssoggy.ssoggysouls.util;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommonCommandUtilTest {
    @Test
    public void testGetCommandPrefix() {
        assertEquals("/test", CommonCommandUtil.getCommandPrefix("/test something"));
        assertEquals("/hello", CommonCommandUtil.getCommandPrefix(" /hello "));
    }
}
