package org.ssoggy.ssoggysouls.database;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseInitializationExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String expectedMessage = "Database connection failed";
        DatabaseInitializationException exception = new DatabaseInitializationException(expectedMessage);

        assertEquals(expectedMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String expectedMessage = "Database connection failed";
        Throwable expectedCause = new RuntimeException("Underlying failure");
        DatabaseInitializationException exception = new DatabaseInitializationException(expectedMessage, expectedCause);

        assertEquals(expectedMessage, exception.getMessage());
        assertEquals(expectedCause, exception.getCause());
    }
}
