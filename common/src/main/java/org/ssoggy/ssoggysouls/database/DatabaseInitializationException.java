package org.ssoggy.ssoggysouls.database;

public class DatabaseInitializationException extends Exception {
    public DatabaseInitializationException(String message) {
        super(message);
    }

    public DatabaseInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
