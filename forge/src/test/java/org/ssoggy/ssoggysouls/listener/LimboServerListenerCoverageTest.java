package org.ssoggy.ssoggysouls.listener;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class LimboServerListenerCoverageTest {
    @Test
    public void testSetDatabase() {
        assertDoesNotThrow(() -> LimboServerListener.setDatabase(null));
    }
}
