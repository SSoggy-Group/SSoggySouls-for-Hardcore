package org.ssoggy.ssoggysouls.database;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import static org.mockito.Mockito.*;

class SqlSafetyTest {

    @Test
    void testIsValidIdentifier() {
        assertTrue(SqlSafety.isValidIdentifier("valid_identifier_123"));
        assertTrue(SqlSafety.isValidIdentifier("Table"));
        assertTrue(SqlSafety.isValidIdentifier("_private"));

        assertFalse(SqlSafety.isValidIdentifier(null));
        assertFalse(SqlSafety.isValidIdentifier(""));
        assertFalse(SqlSafety.isValidIdentifier(" "));
        assertFalse(SqlSafety.isValidIdentifier("invalid identifier"));
        assertFalse(SqlSafety.isValidIdentifier("table; DROP TABLE users"));
        assertFalse(SqlSafety.isValidIdentifier("select * from user"));
        assertFalse(SqlSafety.isValidIdentifier("name' OR '1'='1"));
        assertFalse(SqlSafety.isValidIdentifier("table-name"));
    }

    @Test
    void testRequireIdentifier() {
        assertEquals("valid_identifier", SqlSafety.requireIdentifier("valid_identifier", "Table name"));

        assertThrows(IllegalArgumentException.class, () -> SqlSafety.requireIdentifier(null, "Table name"));
        assertThrows(IllegalArgumentException.class, () -> SqlSafety.requireIdentifier("", "Table name"));
        assertThrows(IllegalArgumentException.class, () -> SqlSafety.requireIdentifier(" ", "Table name"));
        assertThrows(IllegalArgumentException.class, () -> SqlSafety.requireIdentifier("invalid name", "Table name"));
        assertThrows(IllegalArgumentException.class, () -> SqlSafety.requireIdentifier("name;", "Table name"));
    }

    @Test
    void testRequireValidJdbcParam() {
        assertEquals("valid_param-1.2:[]", SqlSafety.requireValidJdbcParam("valid_param-1.2:[]", "Param"));
        assertEquals("12345", SqlSafety.requireValidJdbcParam("12345", "Param"));
        assertEquals("some.url.com:8080", SqlSafety.requireValidJdbcParam("some.url.com:8080", "Param"));

        assertThrows(IllegalArgumentException.class, () -> SqlSafety.requireValidJdbcParam(null, "Param"));
        assertThrows(IllegalArgumentException.class, () -> SqlSafety.requireValidJdbcParam("", "Param"));
        assertThrows(IllegalArgumentException.class, () -> SqlSafety.requireValidJdbcParam(" ", "Param"));
        assertThrows(IllegalArgumentException.class, () -> SqlSafety.requireValidJdbcParam("param;", "Param"));
        assertThrows(IllegalArgumentException.class, () -> SqlSafety.requireValidJdbcParam("param' OR 1=1", "Param"));
        assertThrows(IllegalArgumentException.class, () -> SqlSafety.requireValidJdbcParam("param(", "Param"));
    }

    @Test
    void testConstructor() throws Exception {
        java.lang.reflect.Constructor<SqlSafety> constructor = SqlSafety.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    void testPrepareStatement() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        when(mockConn.prepareStatement("SELECT * FROM valid_table")).thenReturn(mockStmt);

        PreparedStatement result = SqlSafety.prepareStatement(mockConn, "SELECT * FROM valid_table");
        assertEquals(mockStmt, result);
    }
}
