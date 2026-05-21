package org.ssoggy.ssoggysouls.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Level;

import javax.sql.DataSource;

import org.ssoggy.ssoggysouls.PluginContext;
import org.ssoggy.ssoggysouls.model.PlayerData;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class SQLiteManager extends AbstractDatabaseManager {

    private static final int MAX_SQLITE_IN_PARAMS = 900;
    private static final String BIGINT_NOT_NULL_DEFAULT_0 = "BIGINT NOT NULL DEFAULT 0";
    private static final Set<String> ALLOWED_COLUMN_DEFINITIONS = Set.of(
            BIGINT_NOT_NULL_DEFAULT_0
    );

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
            if (!SqlSafety.isValidIdentifier(configuredTableName)) {
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

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + "uuid VARCHAR(36) NOT NULL PRIMARY KEY, "
                + "username VARCHAR(16) NOT NULL, "
                + "lives INT NOT NULL DEFAULT " + plugin.getDefaultLives() + ", "
                + "is_dead BOOLEAN NOT NULL DEFAULT FALSE, "
                + "first_join BIGINT NOT NULL, "
                + "last_death " + BIGINT_NOT_NULL_DEFAULT_0 + ", "
                + "last_seen " + BIGINT_NOT_NULL_DEFAULT_0 + ", "
                + "grace_until " + BIGINT_NOT_NULL_DEFAULT_0
                + ");";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = SqlSafety.prepareStatement(conn, sql)) {
            ps.executeUpdate();
            ensureLastSeenColumn(conn);
            ensureGraceUntilColumn(conn);
            plugin.debug("Table '" + tableName + "' verified/created.");
        }
    }

    private void ensureLastSeenColumn(Connection conn) {
        ensureColumn(conn, "last_seen", BIGINT_NOT_NULL_DEFAULT_0);
    }

    private void ensureGraceUntilColumn(Connection conn) {
        ensureColumn(conn, "grace_until", BIGINT_NOT_NULL_DEFAULT_0);
    }

    /**
     * Ensures a column exists in the table, ignoring duplicate-column errors.
     *
     * @param conn       database connection
     * @param columnName name of the column to add
     * @param definition SQL definition of the column (for example, "BIGINT NOT NULL
     *                   DEFAULT 0")
     */
    private void ensureColumn(Connection conn, String columnName, String definition) {
        String safeColumnName = SqlSafety.requireIdentifier(columnName, "column name");
        if (definition == null) {
            throw new IllegalArgumentException("Column definition cannot be null");
        }
        String normalizedDefinition = definition.trim().replaceAll("\\s+", " ").toUpperCase();
        if (!ALLOWED_COLUMN_DEFINITIONS.contains(normalizedDefinition)) {
            throw new IllegalArgumentException("Column definition is not in allowed whitelist: " + normalizedDefinition);
        }

        String sql = "ALTER TABLE " + tableName + " ADD COLUMN " + safeColumnName + " " + normalizedDefinition;
        try (PreparedStatement ps = SqlSafety.prepareStatement(conn, sql)) {
            ps.executeUpdate();
            plugin.debug("Added " + columnName + " column to '" + tableName + "'.");
        } catch (SQLException e) {
            boolean duplicateColumn = e.getMessage() != null
                    && e.getMessage().toLowerCase().contains("duplicate column name");
            if (!duplicateColumn) {
                plugin.getLogger().log(Level.WARNING, e, () -> "Failed to ensure " + columnName + " column");
            }
        }
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
                plugin.debug("Saved player data: " + data);
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
