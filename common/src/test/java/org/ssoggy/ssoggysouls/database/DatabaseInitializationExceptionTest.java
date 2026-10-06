package org.ssoggy.ssoggysouls.database;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class DatabaseInitializationExceptionTest {

    @Test
    void testMessageOnlyConstructor() {
        DatabaseInitializationException ex = new DatabaseInitializationException("DB error");
        assertEquals("DB error", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void testMessageAndCauseConstructor() {
        Throwable cause = new RuntimeException("Underlying cause");
        DatabaseInitializationException ex = new DatabaseInitializationException("DB error", cause);
        assertEquals("DB error", ex.getMessage());
        assertSame(cause, ex.getCause());
    }
}
