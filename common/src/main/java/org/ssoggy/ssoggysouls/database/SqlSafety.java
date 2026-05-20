package org.ssoggy.ssoggysouls.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Pattern;

final class SqlSafety {

    private static final Pattern IDENTIFIER = Pattern.compile("\\A\\w+\\z");
    private static final Pattern JDBC_PARAM = Pattern.compile("\\A[a-zA-Z0-9_.\\-\\[\\]:]+\\z");

    private SqlSafety() {
    }

    static String requireValidJdbcParam(String param, String label) {
        if (param == null || !JDBC_PARAM.matcher(param).matches()) {
            throw new IllegalArgumentException(
                    label + " must contain only alphanumeric characters, periods, hyphens, underscores, colons, and square brackets");
        }
        return param;
    }

    static String requireIdentifier(String identifier, String label) {
        if (identifier == null || !IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException(label + " must contain only ASCII letters, digits, and underscores");
        }
        return identifier;
    }

    /**
     * Verifies that the given string contains only alphanumeric characters and underscores.
     * This prevents SQL injection when dynamic table names must be used.
     * @param identifier The string to check.
     * @return true if valid, false otherwise.
     */
    public static boolean isValidIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return false;
        }
        return identifier.matches("^[a-zA-Z0-9_]+$");
    }

    @SuppressWarnings("java:S2077")
    static PreparedStatement prepareStatement(Connection conn, String trustedSql) throws SQLException {
        /*
         * SQL identifiers cannot be JDBC parameters. Callers build these statements
         * only from validated identifiers or constants, then bind user values with ?.
         */
        return conn.prepareStatement(trustedSql);
    }
}
