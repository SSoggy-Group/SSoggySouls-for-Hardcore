package org.ssoggy.ssoggysouls.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import javax.sql.DataSource;

import org.ssoggy.ssoggysouls.PluginContext;
import org.ssoggy.ssoggysouls.model.PlayerData;

/**
 * Contains all shared SQL logic for player data management, shared between
 * {@link MySQLManager} and {@link SQLiteManager}. Subclasses provide the
 * {@link DataSource} and override dialect-specific behavior such as
 * {@link #savePlayer(PlayerData)} and schema migration utilities.
 */
public abstract class AbstractDatabaseManager implements DatabaseManager {

    protected static final String COL_IS_DEAD = "is_dead";
    protected static final String SELECT_ALL =
            "SELECT uuid, username, lives, is_dead, first_join, last_death, last_seen, grace_until FROM ";
    protected static final String UPDATE = "UPDATE ";

    protected final PluginContext plugin;
    protected final DeathStatusCache deathStatusCache = new DeathStatusCache();

    /** Resolved and validated table name; set during {@code initialize()}. */
    protected String tableName;

    protected AbstractDatabaseManager(PluginContext plugin) {
        this.plugin = plugin;
    }

    /** Returns the active JDBC {@link DataSource}. */
    protected abstract DataSource getDataSource();

    // -------------------------------------------------------------------------
    // Shared read operations
    // -------------------------------------------------------------------------

    @Override
    public PlayerData getPlayer(UUID uuid) {
        String sql = SELECT_ALL + tableName + " WHERE uuid = ?";
        try (Connection conn = getDataSource().getConnection();
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

    @Override
    public PlayerData getPlayerByName(String username) {
        String sql = SELECT_ALL + tableName + " WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = getDataSource().getConnection();
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

    @Override
    public boolean isPlayerDead(UUID uuid) {
        Boolean cached = deathStatusCache.get(uuid);
        if (cached != null) {
            return cached;
        }
        String sql = "SELECT is_dead FROM " + tableName + " WHERE uuid = ?";
        try (Connection conn = getDataSource().getConnection();
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

    @Override
    public java.util.Map<UUID, Boolean> arePlayersDead(Set<UUID> uuids) {
        Map<UUID, Boolean> result = new java.util.HashMap<>();
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

        // Fail-safe dead default for missing records
        for (UUID uuid : toFetch) {
            result.put(uuid, true);
        }

        List<UUID> toFetchList = new ArrayList<>(toFetch);
        fetchDeathStatusBatch(toFetchList, result);
        return result;
    }

    /**
     * Fetches death status for a list of UUIDs from the database in batches.
     * Subclasses may override to use a different batch size limit.
     */
    protected void fetchDeathStatusBatch(List<UUID> toFetchList, Map<UUID, Boolean> result) {
        int batchSize = getBatchSize();
        try (Connection conn = getDataSource().getConnection()) {
            for (int start = 0; start < toFetchList.size(); start += batchSize) {
                int end = Math.min(start + batchSize, toFetchList.size());
                List<UUID> batch = toFetchList.subList(start, end);

                String placeholders = String.join(",",
                        java.util.Collections.nCopies(batch.size(), "?"));
                String sql = "SELECT uuid, is_dead FROM " + tableName
                        + " WHERE uuid IN (" + placeholders + ")";

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
    }

    /** Maximum number of UUIDs per IN(...) batch. SQLite overrides this to 900. */
    protected int getBatchSize() {
        return 500;
    }

    @Override
    public List<PlayerData> getDeadPlayers() {
        String sql = SELECT_ALL + tableName + " WHERE is_dead = TRUE ORDER BY username";
        List<PlayerData> result = new ArrayList<>();
        try (Connection conn = getDataSource().getConnection();
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

    // -------------------------------------------------------------------------
    // Shared write operations
    // -------------------------------------------------------------------------

    @Override
    public boolean revivePlayer(UUID uuid, int livesToRestore) {
        String sql = UPDATE + tableName
                + " SET is_dead = FALSE, lives = ? WHERE uuid = ? AND is_dead = TRUE";
        try (Connection conn = getDataSource().getConnection();
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

    @Override
    public void setLives(UUID uuid, int lives) {
        String sql = UPDATE + tableName + " SET lives = ?, is_dead = ? WHERE uuid = ?";
        try (Connection conn = getDataSource().getConnection();
                PreparedStatement ps = SqlSafety.prepareStatement(conn, sql)) {
            boolean dead = lives <= 0;
            ps.setInt(1, Math.max(0, lives));
            ps.setBoolean(2, dead);
            ps.setString(3, uuid.toString());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                deathStatusCache.put(uuid, dead);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, e, () -> "Failed to set lives for " + uuid);
        }
    }

    @Override
    public void setFirstJoin(UUID uuid, long firstJoin) {
        String sql = UPDATE + tableName + " SET first_join = ? WHERE uuid = ?";
        try (Connection conn = getDataSource().getConnection();
                PreparedStatement ps = SqlSafety.prepareStatement(conn, sql)) {
            ps.setLong(1, firstJoin);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, e, () -> "Failed to set first_join for " + uuid);
        }
    }

    @Override
    public void setLastSeen(UUID uuid, long lastSeen) {
        String sql = UPDATE + tableName + " SET last_seen = ? WHERE uuid = ?";
        try (Connection conn = getDataSource().getConnection();
                PreparedStatement ps = SqlSafety.prepareStatement(conn, sql)) {
            ps.setLong(1, lastSeen);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, e, () -> "Failed to set last_seen for " + uuid);
        }
    }

    @Override
    public void setGraceUntil(UUID uuid, long graceUntil) {
        String sql = UPDATE + tableName + " SET grace_until = ? WHERE uuid = ?";
        try (Connection conn = getDataSource().getConnection();
                PreparedStatement ps = SqlSafety.prepareStatement(conn, sql)) {
            ps.setLong(1, graceUntil);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, e, () -> "Failed to set grace_until for " + uuid);
        }
    }

    @Override
    public void invalidateDeathStatusCache(UUID uuid) {
        deathStatusCache.remove(uuid);
    }

    // -------------------------------------------------------------------------
    // Shared plugin version metadata
    // -------------------------------------------------------------------------

    @Override
    public String getPluginVersion(String key) {
        String metaTable = "ssoggysouls_meta";
        try (Connection conn = getDataSource().getConnection()) {
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
            plugin.getLogger().log(Level.WARNING, e,
                    () -> "Failed to get plugin version from database for key: " + key);
        }
        return null;
    }

    /**
     * Persists the plugin version for the given key. Subclasses override this to
     * use the correct upsert syntax for their dialect.
     */
    @Override
    public abstract void savePluginVersion(String key, String version);

    /**
     * Creates the metadata table if it does not already exist.
     * Subclasses provide the CREATE TABLE body via
     * {@link #metadataTableDdl(String)}.
     */
    protected void createMetadataTableIfNeeded(Connection conn, String metaTable) throws SQLException {
        SqlSafety.requireIdentifier(metaTable, "metadata table name");
        String createTableSql = metadataTableDdl(metaTable);
        try (PreparedStatement ps = SqlSafety.prepareStatement(conn, createTableSql)) {
            ps.execute();
        }
    }

    /** Returns the dialect-specific CREATE TABLE DDL for the metadata table. */
    protected abstract String metadataTableDdl(String metaTable);

    // -------------------------------------------------------------------------
    // Shared helpers
    // -------------------------------------------------------------------------

    protected PlayerData mapResultSet(ResultSet rs) throws SQLException {
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
}
