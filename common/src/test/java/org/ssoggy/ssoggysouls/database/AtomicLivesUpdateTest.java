package org.ssoggy.ssoggysouls.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sqlite.SQLiteDataSource;
import org.ssoggy.ssoggysouls.PluginContext;
import org.ssoggy.ssoggysouls.model.PlayerData;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.logging.Logger;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Runs the targeted updates against a real SQLite database: the Extra Life dupe fix
 * depends on the conditional UPDATE semantics, which mocks cannot verify.
 */
class AtomicLivesUpdateTest {

    @TempDir
    Path tempDir;

    private SqlDatabaseManager db;
    private final UUID uuid = UUID.randomUUID();

    private static final class SqlDatabaseManager extends AbstractDatabaseManager {
        private final DataSource dataSource;

        SqlDatabaseManager(PluginContext plugin, DataSource dataSource) {
            super(plugin);
            this.dataSource = dataSource;
            this.tableName = "players";
        }

        @Override
        protected DataSource getDataSource() {
            return dataSource;
        }

        @Override
        public void initialize() { /* schema created by the test */ }

        @Override
        public void shutdown() { /* nothing to close */ }

        @Override
        public void savePlayer(PlayerData data) { /* not needed */ }

        @Override
        public void savePluginVersion(String key, String version) { /* not needed */ }

        @Override
        protected String metadataTableDdl(String metaTable) {
            return "";
        }
    }

    @BeforeEach
    void setup() throws SQLException {
        PluginContext plugin = mock(PluginContext.class);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("test"));

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + tempDir.resolve("test.db"));
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE players (uuid TEXT PRIMARY KEY, username TEXT, lives INTEGER, "
                    + "is_dead BOOLEAN, first_join BIGINT, last_death BIGINT, last_seen BIGINT, grace_until BIGINT)");
        }
        db = new SqlDatabaseManager(plugin, dataSource);
    }

    private void insert(int lives, boolean dead) throws SQLException {
        try (Connection conn = db.getDataSource().getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO players VALUES (?, 'Steve', ?, ?, 0, 0, 0, 0)")) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, lives);
            ps.setBoolean(3, dead);
            ps.executeUpdate();
        }
    }

    @Test
    void incrementLivesAddsOneBelowCap() throws SQLException {
        insert(2, false);
        assertTrue(db.incrementLives(uuid, 5));
        assertEquals(3, db.getPlayer(uuid).getLives());
    }

    @Test
    void incrementLivesRefusesAtCap() throws SQLException {
        insert(5, false);
        assertFalse(db.incrementLives(uuid, 5));
        assertEquals(5, db.getPlayer(uuid).getLives());
    }

    @Test
    void incrementLivesUnlimitedWhenCapDisabled() throws SQLException {
        insert(99, false);
        assertTrue(db.incrementLives(uuid, 0));
        assertEquals(100, db.getPlayer(uuid).getLives());
    }

    @Test
    void incrementLivesRefusesDeadPlayer() throws SQLException {
        insert(0, true);
        assertFalse(db.incrementLives(uuid, 5));
        PlayerData data = db.getPlayer(uuid);
        assertEquals(0, data.getLives());
        assertTrue(data.isDead());
    }

    @Test
    void repeatedIncrementsNeverExceedCap() throws SQLException {
        insert(3, false);
        int granted = 0;
        for (int i = 0; i < 5; i++) {
            if (db.incrementLives(uuid, 4)) granted++;
        }
        assertEquals(1, granted);
        assertEquals(4, db.getPlayer(uuid).getLives());
    }

    @Test
    void setUsernameOnlyTouchesUsername() throws SQLException {
        insert(2, false);
        db.setUsername(uuid, "Alex");
        PlayerData data = db.getPlayer(uuid);
        assertEquals("Alex", data.getUsername());
        assertEquals(2, data.getLives());
        assertFalse(data.isDead());
    }
}
