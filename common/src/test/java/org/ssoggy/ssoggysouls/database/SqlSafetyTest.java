package org.ssoggy.ssoggysouls.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SqlSafetyTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "valid_Param",
            "12345",
            "param-with-hyphen",
            "param.with.period",
            "param:with:colon",
            "param[with]brackets",
            "A1_B2.C3-D4:E5[F6]"
    })
    void testRequireValidJdbcParam_Valid(String param) {
        assertEquals(param, SqlSafety.requireValidJdbcParam(param, "TestLabel"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            " ",
            "param with space",
            "param'with'quotes",
            "param\"with\"quotes",
            "param;with;semicolon",
            "param*with*asterisk",
            "param=with=equals",
            "param<with>angle",
            "param\\with\\slash"
    })
    void testRequireValidJdbcParam_Invalid(String param) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> SqlSafety.requireValidJdbcParam(param, "TestLabel"));
        assertEquals(
                "TestLabel must contain only alphanumeric characters, periods, hyphens, underscores, colons, and square brackets",
                ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "valid_Identifier",
            "12345",
            "test_123"
    })
    void testRequireIdentifier_Valid(String identifier) {
        assertEquals(identifier, SqlSafety.requireIdentifier(identifier, "TestLabel"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            " ",
            "param-with-hyphen",
            "param.with.period",
            "param with space",
            "param;drop table;"
    })
    void testRequireIdentifier_Invalid(String identifier) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> SqlSafety.requireIdentifier(identifier, "TestLabel"));
        assertEquals("TestLabel must contain only ASCII letters, digits, and underscores", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "valid_Identifier",
            "12345",
            "test_123"
    })
    void testIsValidIdentifier_True(String identifier) {
        assertTrue(SqlSafety.isValidIdentifier(identifier));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            " ",
            "param-with-hyphen",
            "param.with.period",
            "param with space",
            "param;drop table;"
    })
    void testIsValidIdentifier_False(String identifier) {
        assertFalse(SqlSafety.isValidIdentifier(identifier));
    }

    @Test
    void testConstructor() throws Exception {
        java.lang.reflect.Constructor<SqlSafety> constructor = SqlSafety.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    void testPrepareStatement() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        String sql = "SELECT * FROM users WHERE id = ?";
        when(mockConnection.prepareStatement(sql)).thenReturn(mockStatement);

        PreparedStatement stmt = SqlSafety.prepareStatement(mockConnection, sql);

        assertEquals(mockStatement, stmt);
    }
}
