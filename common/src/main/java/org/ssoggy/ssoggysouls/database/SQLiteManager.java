package org.ssoggy.ssoggysouls.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.ssoggy.ssoggysouls.PluginContext;
import org.ssoggy.ssoggysouls.model.PlayerData;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class SQLiteManager implements DatabaseManager {

    private static final String COL_IS_DEAD = "is_dead";
    private static final String SELECT_ALL = "SELECT uuid, username, lives, is_dead, first_join, last_death, last_seen, grace_until FROM ";
    private static final String UPDATE = "UPDATE ";

    // simple cache for death status with TTL to reduce DB queries
    private static final long CACHE_TTL_MS = 2000; // 2 second cache
    private final Map<UUID, CachedDeathStatus> deathStatusCache = new ConcurrentHashMap<>();

    private final PluginContext plugin;
    private HikariDataSource dataSource;
    private String tableName;

    private static class CachedDeathStatus {
        final boolean isDead;
        final long timestamp;

        CachedDeathStatus(boolean isDead) {
            this.isDead = isDead;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL_MS;
        }
    }

    public SQLiteManager(PluginContext plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() throws DatabaseInitializationException {
        try {
            tableName = plugin.getConfigString("database.table-name", "hardcore_players");
            if (!isValidIdentifier(tableName)) {
                plugin.getLogger().log(Level.SEVERE, "SQLite initialization failed: Invalid database.table-name {0}. Table name must consist only of alphanumeric characters and underscores.", tableName);
                throw new DatabaseInitializationException("Invalid database.table-name: " + tableName);
            }

            java.io.File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            java.io.File dbFile = new java.io.File(dataFolder, "database.db");
            String jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setDriverClassName("org.sqlite.JDBC");
            config.setMaximumPoolSize(1);
            config.setConnectionTimeout(10_000);
            config.setPoolName("SSoggySouls-SQLite-Pool");

            try {
                dataSource = new HikariDataSource(config);
            } catch (RuntimeException ex) {
                plugin.getLogger().log(Level.SEVERE, "SQLite connection pool error:", ex);
                throw new DatabaseInitializationException("Could not create SQLite connection pool", ex);
            }
            createTable();

            plugin.getLogger().log(Level.INFO, "SQLite connection established (database.db)");

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "SQLite initialization failed!", e);
            throw new DatabaseInitializationException("SQLite initialization failed", e);
        }
    }

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
                + "last_death BIGINT NOT NULL DEFAULT 0, "
                + "last_seen BIGINT NOT NULL DEFAULT 0, "
                + "grace_until BIGINT NOT NULL DEFAULT 0"
                + ");";

        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            ensureLastSeenColumn(conn);
            ensureGraceUntilColumn(conn);
            plugin.debug("Table '" + tableName + "' verified/created.");
        }
    }

    private void ensureLastSeenColumn(Connection conn) {
        ensureColumn(conn, "last_seen", "BIGINT NOT NULL DEFAULT 0");
    }

    private void ensureGraceUntilColumn(Connection conn) {
        ensureColumn(conn, "grace_until", "BIGINT NOT NULL DEFAULT 0");
    }

    private boolean isValidIdentifier(String identifier) {
        return identifier != null && identifier.matches("^\\w+$");
    }

    /**
     * ensures a column exists in the table, ignoring duplicate-column errors.
     *
     * @param conn       database connection
     * @param columnName name of the column to add
     * @param definition SQL definition of the column (for example, "BIGINT NOT NULL
     *                   DEFAULT 0")
     */
    private void ensureColumn(Connection conn, String columnName, String definition) {
        if (!isValidIdentifier(columnName)) {
            throw new IllegalArgumentException("Invalid column name identifier: " + columnName);
        }
        if (definition == null || !definition.matches("^[a-zA-Z0-9_ \\(\\),'.\\-]+$")) {
            throw new IllegalArgumentException("Invalid column definition characters: " + definition);
        }
        if (definition.contains("--")) {
            throw new IllegalArgumentException("SQL comments are not allowed in column definition: " + definition);
        }

        String sql = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition;
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            plugin.debug("Added " + columnName + " column to '" + tableName + "'.");
        } catch (SQLException e) {
            boolean duplicateColumn = e.getMessage() != null
                    && e.getMessage().toLowerCase().contains("duplicate column name");
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
                PreparedStatement ps = conn.prepareStatement(sql)) {

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
                PreparedStatement ps = conn.prepareStatement(sql)) {

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
                + "ON CONFLICT (uuid) DO UPDATE SET "
                + "username = excluded.username, "
                + "lives = excluded.lives, "
                + "is_dead = excluded.is_dead, "
                + "last_death = excluded.last_death, "
                + "last_seen = excluded.last_seen, "
                + "grace_until = excluded.grace_until";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, data.getUuid().toString());
            ps.setString(2, data.getUsername());
            ps.setInt(3, data.getLives());
            ps.setBoolean(4, data.isDead());
            ps.setLong(5, data.getFirstJoin());
            ps.setLong(6, data.getLastDeath());
            ps.setLong(7, data.getLastSeen());
            ps.setLong(8, data.getGraceUntil());

            ps.executeUpdate();

            deathStatusCache.remove(data.getUuid());
            if (plugin.isDebugMode()) {
                plugin.debug("Saved player data: " + data);
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, e, () -> "Failed to save player " + data.getUuid());
            deathStatusCache.remove(data.getUuid());
        }
    }


    public java.util.Map<UUID, Boolean> arePlayersDead(java.util.Set<UUID> uuids) {
        java.util.Map<UUID, Boolean> result = new java.util.HashMap<>();
        if (uuids == null || uuids.isEmpty()) return result;

        java.util.Set<UUID> toFetch = new java.util.HashSet<>();
        for (UUID uuid : uuids) {
            CachedDeathStatus cached = deathStatusCache.get(uuid);
            if (cached != null && !cached.isExpired()) {
                result.put(uuid, cached.isDead);
            } else {
                toFetch.add(uuid);
            }
        }

        if (toFetch.isEmpty()) {
            return result;
        }

        // Default missing to true
        for (UUID uuid : toFetch) {
            result.put(uuid, true);
        }

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < toFetch.size(); i++) {
            placeholders.append("?");
            if (i < toFetch.size() - 1) placeholders.append(",");
        }

        String sql = "SELECT uuid, is_dead FROM " + tableName + " WHERE uuid IN (" + placeholders.toString() + ")";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int i = 1;
            for (UUID uuid : toFetch) {
                ps.setString(i++, uuid.toString());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    boolean isDead = rs.getBoolean(COL_IS_DEAD);
                    result.put(uuid, isDead);
                    deathStatusCache.put(uuid, new CachedDeathStatus(isDead));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, e, () -> "Failed to bulk check death status");
        }

        return result;
    }

    public boolean isPlayerDead(UUID uuid) {
        CachedDeathStatus cached = deathStatusCache.get(uuid);
        if (cached != null && !cached.isExpired()) {
            return cached.isDead;
        }
        String sql = "SELECT is_dead FROM " + tableName + " WHERE uuid = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean isDead = rs.getBoolean(COL_IS_DEAD);
                    // Update cache
                    deathStatusCache.put(uuid, new CachedDeathStatus(isDead));
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
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, livesToRestore);
            ps.setString(2, uuid.toString());

            int rows = ps.executeUpdate();

            // invalidate cache on death status change
            if (rows > 0) {
                deathStatusCache.remove(uuid);
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
                PreparedStatement ps = conn.prepareStatement(sql)) {

            boolean dead = lives <= 0;
            ps.setInt(1, Math.max(0, lives));
            ps.setBoolean(2, dead);
            ps.setString(3, uuid.toString());

            ps.executeUpdate();

            // invalidate cache on death status change again
            deathStatusCache.remove(uuid);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, e, () -> "Failed to set lives for " + uuid);
        }
    }

    public void setFirstJoin(UUID uuid, long firstJoin) {
        String sql = UPDATE + tableName + " SET first_join = ? WHERE uuid = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

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
                PreparedStatement ps = conn.prepareStatement(sql)) {

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
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, graceUntil);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, e, () -> "Failed to set grace_until for " + uuid);
        }
    }

    public void invalidateDeathStatusCache(UUID uuid) {
        deathStatusCache.remove(uuid);
    }

    public List<PlayerData> getDeadPlayers() {
        String sql = SELECT_ALL + tableName + " WHERE is_dead = TRUE ORDER BY username";

        List<PlayerData> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, e, () -> "Failed to get dead players");
        }
        return result;
    }

    // gets plugin version from db, returns null if first time running
    // The key parameter allows tracking different versions per server role
    // (main/limbo)
    public String getPluginVersion(String key) {
        String metaTable = "ssoggysouls_meta";
        try (Connection conn = dataSource.getConnection()) {
            // Check if metadata table exists (and create if needed)
            createMetadataTableIfNeeded(conn, metaTable);

            String sql = "SELECT version FROM " + metaTable + " WHERE key_ = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
                    + "ON CONFLICT (key_) DO UPDATE SET version = excluded.version";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, key);
                ps.setString(2, version);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, e, () -> "Failed to save plugin version to database for key: " + key);
        }
    }

    private void createMetadataTableIfNeeded(Connection conn, String metaTable) throws SQLException {
        String createTableSql = "CREATE TABLE IF NOT EXISTS " + metaTable + " ("
                + "key_ VARCHAR(50) PRIMARY KEY,"
                + "version VARCHAR(50)"
                + ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSql);
        }
    }
}
