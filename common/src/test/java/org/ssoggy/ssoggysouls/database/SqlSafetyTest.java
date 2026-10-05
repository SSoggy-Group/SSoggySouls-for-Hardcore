package org.ssoggy.ssoggysouls.database;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SqlSafetyTest {

    @Test
    void testRequireValidJdbcParam_valid() {
        assertEquals("localhost", SqlSafety.requireValidJdbcParam("localhost", "host"));
        assertEquals("127.0.0.1", SqlSafety.requireValidJdbcParam("127.0.0.1", "host"));
        assertEquals("db_name", SqlSafety.requireValidJdbcParam("db_name", "database"));
        assertEquals("my-host:3306", SqlSafety.requireValidJdbcParam("my-host:3306", "address"));
        assertEquals("[::1]", SqlSafety.requireValidJdbcParam("[::1]", "ipv6"));
    }

    @Test
    void testRequireValidJdbcParam_invalid() {
        assertThrows(IllegalArgumentException.class, () -> SqlSafety.requireValidJdbcParam(null, "host"));
        assertThrows(IllegalArgumentException.class, () -> SqlSafety.requireValidJdbcParam("localhost; DROP TABLE", "host"));
        assertThrows(IllegalArgumentException.class, () -> SqlSafety.requireValidJdbcParam("db' OR '1'='1", "database"));
        assertThrows(IllegalArgumentException.class, () -> SqlSafety.requireValidJdbcParam("has space", "param"));
        assertThrows(IllegalArgumentException.class, () -> SqlSafety.requireValidJdbcParam("slash/path", "param"));
    }

    @Test
    void testRequireIdentifier_valid() {
        assertEquals("users", SqlSafety.requireIdentifier("users", "table"));
        assertEquals("table_123", SqlSafety.requireIdentifier("table_123", "table"));
        assertEquals("ssoggy_souls", SqlSafety.requireIdentifier("ssoggy_souls", "table"));
    }

    @Test
    void testRequireIdentifier_invalid() {
        assertThrows(IllegalArgumentException.class, () -> SqlSafety.requireIdentifier(null, "table"));
        assertThrows(IllegalArgumentException.class, () -> SqlSafety.requireIdentifier("", "table"));
        assertThrows(IllegalArgumentException.class, () -> SqlSafety.requireIdentifier("table-name", "table"));
        assertThrows(IllegalArgumentException.class, () -> SqlSafety.requireIdentifier("table.name", "table"));
        assertThrows(IllegalArgumentException.class, () -> SqlSafety.requireIdentifier("table; DROP TABLE users;", "table"));
        assertThrows(IllegalArgumentException.class, () -> SqlSafety.requireIdentifier("table name", "table"));
    }

    @Test
    void testIsIdentifier() {
        assertTrue(SqlSafety.isIdentifier("valid_table_1"));
        assertFalse(SqlSafety.isIdentifier(null));
        assertFalse(SqlSafety.isIdentifier(""));
        assertFalse(SqlSafety.isIdentifier("invalid-identifier"));
        assertFalse(SqlSafety.isIdentifier("with space"));
        assertFalse(SqlSafety.isIdentifier("drop;"));
    }

    @Test
    void testPrepareStatement() throws SQLException {
        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        String sql = "SELECT * FROM valid_table WHERE uuid = ?";
        when(conn.prepareStatement(sql)).thenReturn(stmt);

        PreparedStatement result = SqlSafety.prepareStatement(conn, sql);
        assertNotNull(result);
        assertEquals(stmt, result);
        verify(conn).prepareStatement(sql);
    }
}
