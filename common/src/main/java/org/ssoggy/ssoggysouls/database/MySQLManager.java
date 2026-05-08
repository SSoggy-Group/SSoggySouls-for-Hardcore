package org.ssoggy.ssoggysouls.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.ssoggy.ssoggysouls.PluginContext;
import org.ssoggy.ssoggysouls.model.PlayerData;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class MySQLManager implements DatabaseManager {

    private static final String COL_IS_DEAD = "is_dead";
    private static final String SELECT_ALL = "SELECT uuid, username, lives, is_dead, first_join, last_death, last_seen, grace_until FROM ";
    private static final String UPDATE = "UPDATE ";
    private static final int MYSQL_DUPLICATE_COLUMN = 1060;
    private static final String BIGINT_NOT_NULL_DEFAULT_0 = "BIGINT NOT NULL DEFAULT 0";
    private static final Set<String> ALLOWED_COLUMN_DEFINITIONS = Set.of(
            BIGINT_NOT_NULL_DEFAULT_0
    );

    private final DeathStatusCache deathStatusCache = new DeathStatusCache();

    private final PluginContext plugin;
    private javax.sql.DataSource dataSource;
    private HikariDataSource hikariDataSource; // kept for shutdown()
    private String tableName;

    public MySQLManager(PluginContext plugin) {
        this.plugin = plugin;
    }

    // Package-private constructor for testing / dependency injection
    MySQLManager(PluginContext plugin, javax.sql.DataSource dataSource, String tableName) {
        this.plugin = plugin;
        this.dataSource = dataSource;
        this.tableName = SqlSafety.requireIdentifier(tableName, "table name");
    }

    public void initialize() throws DatabaseInitializationException {
        try {
            String host = plugin.getConfigString("database.host", "localhost");
            int port = plugin.getConfigInt("database.port", 3306);
            String dbName = plugin.getConfigString("database.name", "minecraft");
            String user = plugin.getConfigString("database.username", "minecraft");
            String pass = plugin.getConfigString("database.password", "changeme");
            int poolSize = plugin.getConfigInt("database.pool-size", 5);
            String configuredTableName = plugin.getConfigString("database.table-name", "hardcore_players");
            String sslMode = plugin.getConfigString("database.ssl-mode", "VERIFY_IDENTITY");
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
            String sqlState = e.getSQLState();
            boolean duplicateColumn = e.getErrorCode() == MYSQL_DUPLICATE_COLUMN
                    || "42S21".equals(sqlState);
            if (!duplicateColumn) {
                plugin.getLogger().log(Level.WARNING, e, () -> "Failed to ensure " + columnName + " column");
            }
        }
    }

    private PlayerData mapResultSet(ResultSet rs) throws SQLException {
        return new PlayerData(
                UUID.fromString(rs.getString("uuid")),
                rs.getString("username"),
                rs.getInt("lives"),
                rs.getBoolean(COL_IS_DEAD),
                rs.getLong("first_join"),
                rs.getLong("last_death"),
                rs.getLong("last_seen"),
                rs.getLong("grace_until"));
    }

    public PlayerData getPlayer(UUID uuid) {
        String sql = SELECT_ALL + tableName + " WHERE uuid = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = SqlSafety.prepareStatement(conn, sql)) {

            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, e, () -> "Failed to get player " + uuid);
        }
        return null;
    }

    public PlayerData getPlayerByName(String username) {
        String sql = SELECT_ALL + tableName + " WHERE LOWER(username) = LOWER(?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = SqlSafety.prepareStatement(conn, sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, e, () -> "Failed to get player by name: " + username);
        }
        return null;
    }

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


    public java.util.Map<UUID, Boolean> arePlayersDead(java.util.Set<UUID> uuids) {
        java.util.Map<UUID, Boolean> result = new java.util.HashMap<>();
        if (uuids == null || uuids.isEmpty()) return result;

        java.util.Set<UUID> toFetch = new java.util.HashSet<>();
        for (UUID uuid : uuids) {
            Boolean cached = deathStatusCache.get(uuid);
            if (cached != null) {
                result.put(uuid, cached);
            } else {
                toFetch.add(uuid);
            }
        }

        if (toFetch.isEmpty()) {
            return result;
        }

        // Default missing to true (fail-safe dead default)
        for (UUID uuid : toFetch) {
            result.put(uuid, true);
        }

        final int BATCH_SIZE = 500;
        List<UUID> toFetchList = new ArrayList<>(toFetch);

        try (Connection conn = dataSource.getConnection()) {
            for (int start = 0; start < toFetchList.size(); start += BATCH_SIZE) {
                int end = Math.min(start + BATCH_SIZE, toFetchList.size());
                List<UUID> batch = toFetchList.subList(start, end);

                String placeholders = String.join(",", java.util.Collections.nCopies(batch.size(), "?"));

                String sql = "SELECT uuid, is_dead FROM " + tableName + " WHERE uuid IN (" + placeholders + ")";

                try (PreparedStatement ps = SqlSafety.prepareStatement(conn, sql)) {
                    int i = 1;
                    for (UUID uuid : batch) {
                        ps.setString(i++, uuid.toString());
                    }

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            UUID uuid = UUID.fromString(rs.getString("uuid"));
                            boolean isDead = rs.getBoolean(COL_IS_DEAD);
                            result.put(uuid, isDead);
                            deathStatusCache.put(uuid, isDead);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, e, () -> "Failed to bulk check death status");
        }

        return result;
    }

    public boolean isPlayerDead(UUID uuid) {
        Boolean cached = deathStatusCache.get(uuid);
        if (cached != null) {
            return cached;
        }

        String sql = "SELECT is_dead FROM " + tableName + " WHERE uuid = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = SqlSafety.prepareStatement(conn, sql)) {

            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean isDead = rs.getBoolean(COL_IS_DEAD);
                    deathStatusCache.put(uuid, isDead);
                    return isDead;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, e, () -> "Failed to check death status for " + uuid);
        }
        return true;
    }



    public boolean revivePlayer(UUID uuid, int livesToRestore) {
        String sql = UPDATE + tableName
                + " SET is_dead = FALSE, lives = ? WHERE uuid = ? AND is_dead = TRUE";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = SqlSafety.prepareStatement(conn, sql)) {

            ps.setInt(1, livesToRestore);
            ps.setString(2, uuid.toString());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                deathStatusCache.put(uuid, false);
            }
            if (plugin.isDebugMode()) {
                plugin.debug("Revived player " + uuid + " (rows affected: " + rows + ")");
            }
            return rows > 0;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, e, () -> "Failed to revive player " + uuid);
            return false;
        }
    }

    public void setLives(UUID uuid, int lives) {
        String sql = UPDATE + tableName + " SET lives = ?, is_dead = ? WHERE uuid = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = SqlSafety.prepareStatement(conn, sql)) {

            boolean dead = lives <= 0;
            ps.setInt(1, Math.max(0, lives));
            ps.setBoolean(2, dead);
            ps.setString(3, uuid.toString());

            ps.executeUpdate();

            deathStatusCache.put(uuid, dead);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, e, () -> "Failed to set lives for " + uuid);
        }
    }

    public void setFirstJoin(UUID uuid, long firstJoin) {
        String sql = UPDATE + tableName + " SET first_join = ? WHERE uuid = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = SqlSafety.prepareStatement(conn, sql)) {

            ps.setLong(1, firstJoin);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, e, () -> "Failed to set first_join for " + uuid);
        }
    }

    public void setLastSeen(UUID uuid, long lastSeen) {
        String sql = UPDATE + tableName + " SET last_seen = ? WHERE uuid = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = SqlSafety.prepareStatement(conn, sql)) {

            ps.setLong(1, lastSeen);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, e, () -> "Failed to set last_seen for " + uuid);
        }
    }

    public void setGraceUntil(UUID uuid, long graceUntil) {
        String sql = UPDATE + tableName + " SET grace_until = ? WHERE uuid = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = SqlSafety.prepareStatement(conn, sql)) {

            ps.setLong(1, graceUntil);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, e, () -> "Failed to set grace_until for " + uuid);
        }
    }

    /**
     * manually invalidates a player's death status cache entry.
     * use this when external changes bypass savePlayer(), revivePlayer(), or
     * setLives().
     */
    public void invalidateDeathStatusCache(UUID uuid) {
        if (uuid != null) {
            deathStatusCache.remove(uuid);
        }
    }

    public List<PlayerData> getDeadPlayers() {
        String sql = SELECT_ALL + tableName + " WHERE is_dead = TRUE ORDER BY username";

        List<PlayerData> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = SqlSafety.prepareStatement(conn, sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, e, () -> "Failed to get dead players");
        }
        return result;
    }

    // Gets plugin version from db, returns null if first time running.
    // The key parameter allows tracking different versions per server role
    // (main/limbo)
    public String getPluginVersion(String key) {
        String metaTable = "ssoggysouls_meta";
        try (Connection conn = dataSource.getConnection()) {
            createMetadataTableIfNeeded(conn, metaTable);

            String sql = "SELECT version FROM " + metaTable + " WHERE key_ = ?";
            try (PreparedStatement ps = SqlSafety.prepareStatement(conn, sql)) {
                ps.setString(1, key);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("version");
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, e, () -> "Failed to get plugin version from database for key: " + key);
        }
        return null;
    }

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

    private void createMetadataTableIfNeeded(Connection conn, String metaTable) throws SQLException {
        String safeTableName = SqlSafety.requireIdentifier(metaTable, "metadata table name");
        String createTableSql = "CREATE TABLE IF NOT EXISTS " + safeTableName + " ("
                + "key_ VARCHAR(50) PRIMARY KEY,"
                + "version VARCHAR(50)"
                + ") DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        try (PreparedStatement ps = SqlSafety.prepareStatement(conn, createTableSql)) {
            ps.execute();
        }
    }
}
