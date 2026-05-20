package org.ssoggy.ssoggysouls.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            SqlSafety.requireValidJdbcParam(param, "TestLabel");
        });
        assertTrue(ex.getMessage().contains("TestLabel must contain only alphanumeric"));
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
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            SqlSafety.requireIdentifier(identifier, "TestLabel");
        });
        assertTrue(ex.getMessage().contains("TestLabel must contain only ASCII letters, digits, and underscores"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "valid_Identifier",
            "12345",
            "test_123"
    })
    void testIsIdentifier_True(String identifier) {
        assertTrue(SqlSafety.isIdentifier(identifier));
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
    void testIsIdentifier_False(String identifier) {
        assertFalse(SqlSafety.isIdentifier(identifier));
    }

    @Test
    void testPrepareStatement() throws SQLException {
        Connection mockConnection = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("prepareStatement") && args.length == 1) {
                        return createMockPreparedStatement((String) args[0]);
                    }
                    throw new UnsupportedOperationException("Not implemented");
                }
        );

        String sql = "SELECT * FROM users WHERE id = ?";
        PreparedStatement stmt = SqlSafety.prepareStatement(mockConnection, sql);

        assertEquals("MockPreparedStatement[" + sql + "]", stmt.toString());
    }

    private PreparedStatement createMockPreparedStatement(String sql) {
        return (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class<?>[]{PreparedStatement.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("toString")) {
                        return "MockPreparedStatement[" + sql + "]";
                    }
                    throw new UnsupportedOperationException("Not implemented");
                }
        );
    }
}
