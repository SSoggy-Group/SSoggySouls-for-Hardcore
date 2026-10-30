package org.ssoggy.ssoggysouls.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

import javax.sql.DataSource;

import org.ssoggy.ssoggysouls.PluginContext;
import org.ssoggy.ssoggysouls.model.PlayerData;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class SQLiteManager extends AbstractDatabaseManager {

    private static final int MAX_SQLITE_IN_PARAMS = 900;

    private HikariDataSource dataSource;

    public SQLiteManager(PluginContext plugin) {
        super(plugin);
    }

    @Override
    protected DataSource getDataSource() {
        return dataSource;
    }

    @Override
    protected int getBatchSize() {
        return MAX_SQLITE_IN_PARAMS;
    }

    @Override
    public void initialize() throws DatabaseInitializationException {
        try {
            String configuredTableName = plugin.getConfigString("database.table-name", "hardcore_players");
            if (!SqlSafety.isIdentifier(configuredTableName)) {
                plugin.getLogger().log(Level.SEVERE, "SQLite initialization failed: Invalid database.table-name {0}. Table name must consist only of alphanumeric characters and underscores.", configuredTableName);
                throw new DatabaseInitializationException("Invalid database.table-name: " + configuredTableName);
            }
            tableName = SqlSafety.requireIdentifier(configuredTableName, "database.table-name");

            java.io.File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            java.io.File dbFile = new java.io.File(dataFolder, "database.db");
            String jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setDriverClassName("org.sqlite.JDBC");
        // Keep SQLite pool configurable: writes are serialized, but a larger pool can
        // reduce read-side contention under concurrent access patterns.
            int maxPoolSize = Math.max(1, plugin.getConfigInt("database.max-pool-size", 1));
            config.setMaximumPoolSize(maxPoolSize);
            config.setConnectionTimeout(10_000);
            config.setPoolName("SSoggySouls-SQLite-Pool");

            createHikariDataSource(config);
            createTable();

            plugin.getLogger().log(Level.INFO, "SQLite connection established (database.db)");

        } catch (SQLException | IllegalArgumentException e) {
            plugin.getLogger().log(Level.SEVERE, "SQLite initialization failed!", e);
            throw new DatabaseInitializationException("SQLite initialization failed", e);
        }
    }

    private void createHikariDataSource(HikariConfig config) throws DatabaseInitializationException {
        try {
            dataSource = new HikariDataSource(config);
        } catch (RuntimeException ex) {
            plugin.getLogger().log(Level.SEVERE, "SQLite connection pool error:", ex);
            throw new DatabaseInitializationException("Could not create SQLite connection pool", ex);
        }
    }

    @Override
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("SQLite connection pool closed.");
        }
    }

    @Override
    protected String getCreateTableSql(String safeTableName) {
        return "CREATE TABLE IF NOT EXISTS " + safeTableName + " ("
                + "uuid VARCHAR(36) NOT NULL PRIMARY KEY, "
                + "username VARCHAR(16) NOT NULL, "
                + "lives INT NOT NULL DEFAULT " + plugin.getDefaultLives() + ", "
                + "is_dead BOOLEAN NOT NULL DEFAULT FALSE, "
                + "first_join BIGINT NOT NULL, "
                + "last_death " + BIGINT_NOT_NULL_DEFAULT_0 + ", "
                + "last_seen " + BIGINT_NOT_NULL_DEFAULT_0 + ", "
                + "grace_until " + BIGINT_NOT_NULL_DEFAULT_0
                + ");";
    }

    @Override
    protected boolean isDuplicateColumnException(SQLException e) {
        return e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate column name");
    }

    // SQLite-specific upsert: single parameter set using ON CONFLICT ... DO UPDATE
    @Override
    public void savePlayer(PlayerData data) {
        String sql = "INSERT INTO " + tableName
                + " (uuid, username, lives, is_dead, first_join, last_death, last_seen, grace_until) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT (uuid) DO UPDATE SET "
                + "username = excluded.username, "
                + "lives = excluded.lives, "
                + "is_dead = excluded.is_dead, "
                + "last_death = excluded.last_death, "
                + "last_seen = excluded.last_seen, "
                + "grace_until = excluded.grace_until";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = SqlSafety.prepareStatement(conn, sql)) {

            ps.setString(1, data.getUuid().toString());
            ps.setString(2, data.getUsername());
            ps.setInt(3, data.getLives());
            ps.setBoolean(4, data.isDead());
            ps.setLong(5, data.getFirstJoin());
            ps.setLong(6, data.getLastDeath());
            ps.setLong(7, data.getLastSeen());
            ps.setLong(8, data.getGraceUntil());

            ps.executeUpdate();

            deathStatusCache.put(data.getUuid(), data.isDead());
            if (plugin.isDebugMode()) {
                plugin.debug("Saved player data for " + data.getUuid());
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, e, () -> "Failed to save player " + data.getUuid());
            deathStatusCache.remove(data.getUuid());
        }
    }

    // SQLite-specific upsert for plugin version
    @Override
    public void savePluginVersion(String key, String version) {
        String metaTable = "ssoggysouls_meta";
        try (Connection conn = dataSource.getConnection()) {
            createMetadataTableIfNeeded(conn, metaTable);
            String sql = "INSERT INTO " + metaTable + " (key_, version) VALUES (?, ?) "
                    + "ON CONFLICT (key_) DO UPDATE SET version = excluded.version";
            try (PreparedStatement ps = SqlSafety.prepareStatement(conn, sql)) {
                ps.setString(1, key);
                ps.setString(2, version);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, e, () -> "Failed to save plugin version to database for key: " + key);
        }
    }

    @Override
    protected String metadataTableDdl(String metaTable) {
        return "CREATE TABLE IF NOT EXISTS " + metaTable + " ("
                + "key_ VARCHAR(50) PRIMARY KEY,"
                + "version VARCHAR(50)"
                + ");";
    }
}
