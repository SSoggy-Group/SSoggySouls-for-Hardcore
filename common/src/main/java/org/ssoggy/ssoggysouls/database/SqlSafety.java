package org.ssoggy.ssoggysouls.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Pattern;

final class SqlSafety {

    private static final Pattern IDENTIFIER = Pattern.compile("\\A[A-Za-z0-9_]+\\z");
    private static final Pattern JDBC_PARAM = Pattern.compile("^[a-zA-Z0-9_.\\-]+$");

    private SqlSafety() {
    }

    static String requireIdentifier(String identifier, String label) {
        if (identifier == null || !IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException(label + " must contain only ASCII letters, digits, and underscores");
        }
        return identifier;
    }

    static boolean isIdentifier(String identifier) {
        return identifier != null && IDENTIFIER.matcher(identifier).matches();
    }

    static String requireValidJdbcParam(String value, String label) {
        if (value == null || !JDBC_PARAM.matcher(value).matches()) {
            throw new IllegalArgumentException(label + " must contain only alphanumeric characters, underscores, hyphens, and periods");
        }
        return value;
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
