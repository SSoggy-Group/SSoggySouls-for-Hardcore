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

public class MySQLManager extends AbstractDatabaseManager {

    private static final int MYSQL_DUPLICATE_COLUMN = 1060;
    private static final String BIGINT_NOT_NULL_DEFAULT_0 = "BIGINT NOT NULL DEFAULT 0";
    private static final Set<String> ALLOWED_COLUMN_DEFINITIONS = Set.of(
            BIGINT_NOT_NULL_DEFAULT_0
    );

    private javax.sql.DataSource dataSource;
    private HikariDataSource hikariDataSource; // kept for shutdown()

    public MySQLManager(PluginContext plugin) {
        super(plugin);
    }

    // Package-private constructor for testing / dependency injection
    MySQLManager(PluginContext plugin, javax.sql.DataSource dataSource, String tableName) {
        super(plugin);
        this.dataSource = dataSource;
        this.tableName = SqlSafety.requireIdentifier(tableName, "table name");
    }

    @Override
    protected DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public void initialize() throws DatabaseInitializationException {
        try {
            String host = SqlSafety.requireValidJdbcParam(plugin.getConfigString("database.host", "localhost"), "database.host");
            int port = plugin.getConfigInt("database.port", 3306);
            String dbName = SqlSafety.requireValidJdbcParam(plugin.getConfigString("database.name", "minecraft"), "database.name");
            String user = plugin.getConfigString("database.username", "minecraft");
            String pass = plugin.getConfigString("database.password", "changeme");
            int poolSize = plugin.getConfigInt("database.pool-size", 5);
            String configuredTableName = plugin.getConfigString("database.table-name", "hardcore_players");
            String sslMode = SqlSafety.requireValidJdbcParam(plugin.getConfigString("database.ssl-mode", "VERIFY_IDENTITY"), "database.ssl-mode");
            if (!SqlSafety.isIdentifier(configuredTableName)) {
                plugin.getLogger().log(Level.SEVERE, "MySQL initialization failed: Invalid database.table-name {0}. Table name must consist only of alphanumeric characters and underscores.", configuredTableName);
                throw new DatabaseInitializationException("Invalid database.table-name: " + configuredTableName);
            }
            tableName = SqlSafety.requireIdentifier(configuredTableName, "database.table-name");

            String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                    + "?sslMode=" + sslMode
                    + "&characterEncoding=UTF-8&useUnicode=true";

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(user);
            config.setPassword(pass);
            config.setMaximumPoolSize(poolSize);
            config.setMinimumIdle(1);
            config.setConnectionTimeout(10_000);
            config.setIdleTimeout(300_000);
            config.setMaxLifetime(600_000);
            config.setPoolName("SSoggySouls-Pool");

            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "64");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            createHikariDataSource(config);

            dataSource = hikariDataSource;
            createTable();

            plugin.getLogger().log(Level.INFO, "MySQL connection established ({0}:{1}/{2})",
                    new Object[] { host, port, dbName });

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "MySQL initialization failed!", e);
            throw new DatabaseInitializationException("MySQL initialization failed", e);
        }
    }

    private void createHikariDataSource(HikariConfig config) throws DatabaseInitializationException {
        try {
            hikariDataSource = new HikariDataSource(config);
        } catch (RuntimeException ex) {
            plugin.getLogger().severe("=====================================================");
            plugin.getLogger().severe("SEVERE: Could not connect to the MySQL database. The plugin will be disabled.");
            plugin.getLogger().severe("Please check your connection details in config.yml and see the server log for the full error.");
            plugin.getLogger().severe("NOTICE: If you are only running a single server, you DO NOT need MySQL! The default database is SQLite. Open config.yml and change type: \"mysql\" back to type: \"sqlite\" to fix this instantly.");
            plugin.getLogger().severe("=====================================================");
            plugin.getLogger().log(Level.SEVERE, "MySQL connection error:", ex);
            throw new DatabaseInitializationException("Could not connect to MySQL database", ex);
        }
    }

    @Override
    public void shutdown() {
        if (hikariDataSource != null && !hikariDataSource.isClosed()) {
            hikariDataSource.close();
            plugin.getLogger().info("MySQL connection pool closed.");
        }
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + "uuid VARCHAR(36) NOT NULL PRIMARY KEY, "
                + "username VARCHAR(16) NOT NULL, "
                + "lives INT NOT NULL DEFAULT " + plugin.getDefaultLives() + ", "
                + "is_dead BOOLEAN NOT NULL DEFAULT FALSE, "
                + "first_join BIGINT NOT NULL, "
                + "last_death BIGINT NOT NULL DEFAULT 0, "
                + "last_seen BIGINT NOT NULL DEFAULT 0, "
                + "grace_until BIGINT NOT NULL DEFAULT 0"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

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
            String sqlState = e.getSQLState();
            boolean duplicateColumn = e.getErrorCode() == MYSQL_DUPLICATE_COLUMN
                    || "42S21".equals(sqlState);
            if (!duplicateColumn) {
                plugin.getLogger().log(Level.WARNING, e, () -> "Failed to ensure " + columnName + " column");
            }
        }
    }

    // MySQL-specific upsert: two parameter sets for INSERT + ON DUPLICATE KEY UPDATE
    @Override
    public void savePlayer(PlayerData data) {
        String sql = "INSERT INTO " + tableName
                + " (uuid, username, lives, is_dead, first_join, last_death, last_seen, grace_until) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "username = ?, "
                + "lives = ?, "
                + "is_dead = ?, "
                + "last_death = ?, "
                + "last_seen = ?, "
                + "grace_until = ?";

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
            ps.setString(9, data.getUsername());
            ps.setInt(10, data.getLives());
            ps.setBoolean(11, data.isDead());
            ps.setLong(12, data.getLastDeath());
            ps.setLong(13, data.getLastSeen());
            ps.setLong(14, data.getGraceUntil());

            ps.executeUpdate();
            deathStatusCache.put(data.getUuid(), data.isDead());

            if (plugin.isDebugMode()) {
                plugin.debug("Saved player data: " + data);
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, e, () -> "Failed to save player " + data.getUuid());
            invalidateDeathStatusCache(data.getUuid());
        }
    }

    // MySQL-specific upsert for plugin version
    @Override
    public void savePluginVersion(String key, String version) {
        String metaTable = "ssoggysouls_meta";
        try (Connection conn = dataSource.getConnection()) {
            createMetadataTableIfNeeded(conn, metaTable);
            String sql = "INSERT INTO " + metaTable + " (key_, version) VALUES (?, ?) "
                    + "ON DUPLICATE KEY UPDATE version = ?";
            try (PreparedStatement ps = SqlSafety.prepareStatement(conn, sql)) {
                ps.setString(1, key);
                ps.setString(2, version);
                ps.setString(3, version);
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
                + ") DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
    }
}
