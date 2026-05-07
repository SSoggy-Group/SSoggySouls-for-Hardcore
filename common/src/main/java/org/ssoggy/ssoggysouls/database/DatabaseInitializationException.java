package org.ssoggy.ssoggysouls.database;

/**
 * Ported from https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore/pull/111
 * so platform startup code can distinguish database initialization failures.
 */
public class DatabaseInitializationException extends Exception {
    public DatabaseInitializationException(String message) {
        super(message);
    }

    public DatabaseInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
