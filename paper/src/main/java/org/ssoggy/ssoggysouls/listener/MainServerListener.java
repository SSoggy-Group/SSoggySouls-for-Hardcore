package org.ssoggy.ssoggysouls.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitTask;

import org.ssoggy.ssoggysouls.SSoggySouls;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.model.PlayerData;
import org.ssoggy.ssoggysouls.util.MessageUtil;
import org.ssoggy.ssoggysouls.util.ServerTransferUtil;
import org.ssoggy.ssoggysouls.task.MainReviveCheckTask;

public class MainServerListener implements Listener {

    private static final String PERM_BYPASS = "ssoggysouls.bypass";
    private static final String MSG_SENT_TO_LIMBO = "death-sent-to-limbo";
    private static final String MSG_NOW_SPECTATOR = "death-now-spectator";

    private final SSoggySouls plugin;
    private final DatabaseManager db;
    private final MainReviveCheckTask mainReviveCheckTask;
    
    // Cache frequently accessed config values to avoid repeated lookups
    private String cachedDeathMode;
    private int cachedHybridTimeout;
    
    private final Set<UUID> pendingLimbo = ConcurrentHashMap.newKeySet();
    private final Set<UUID> pendingSurvivalRestore = ConcurrentHashMap.newKeySet();
    private final Set<UUID> expectedGamemodeChanges = ConcurrentHashMap.newKeySet();
    private final Set<UUID> hybridWindowUsed = ConcurrentHashMap.newKeySet();
    // Deaths whose outcome (life lost vs. final death) is still being resolved asynchronously
    private final Set<UUID> deathResultPending = ConcurrentHashMap.newKeySet();
    // Players who clicked respawn before their death outcome was known
    private final Set<UUID> respawnedBeforeResult = ConcurrentHashMap.newKeySet();
    private final Map<UUID, BukkitTask> hybridPendingTransfers = new HashMap<>();
    private final Map<UUID, Long> reviveCooldowns = new ConcurrentHashMap<>();

    public MainServerListener(SSoggySouls plugin, MainReviveCheckTask mainReviveCheckTask) {
        this.plugin = plugin;
        this.db = plugin.getDatabaseManager();
        this.mainReviveCheckTask = mainReviveCheckTask;
        // Initialize cached config values
        refreshConfigCache();
    }
    
    /**
     * refreshes cached config values (call on config reload).
     */
    public void refreshConfigCache() {
        this.cachedDeathMode = plugin.getDeathMode();
        this.cachedHybridTimeout = plugin.getHybridTimeoutSeconds();
    }

    private String effectiveDeathMode() {
        return plugin.isSingleServerMode() ? SSoggySouls.MODE_SPECTATOR : cachedDeathMode;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(PERM_BYPASS)) {
            if (plugin.isDebugMode()) {
                plugin.debug(player.getName() + " has bypass permission, skipping checks.");
            }
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> handleJoinAsync(player));
    }

    private void handleJoinAsync(Player player) {
        org.bukkit.Location pendingRevival = org.ssoggy.ssoggysouls.hrm.RevivalStructureListener.consumePendingRevival(player.getUniqueId());
        if (pendingRevival != null) {
            // Must run synchronous Bukkit API calls on main thread
            Bukkit.getScheduler().runTask(plugin, () ->
                org.ssoggy.ssoggysouls.hrm.RevivalStructureListener.restoreAtStructure(player, pendingRevival));
            // Update database data to reflect revival if necessary, though it is usually saved prior by RevivalStructureListener
            PlayerData data = db.getPlayer(player.getUniqueId());
            if (data != null && data.isDead()) {
                 db.revivePlayer(player.getUniqueId(), plugin.getLivesOnRevive());
            }
        }

        PlayerData data;
        try {
            data = db.getPlayerStrict(player.getUniqueId());
        } catch (java.sql.SQLException e) {
            // A failed read is not a first join: creating a record would overwrite the real one
            plugin.getLogger().log(Level.WARNING, e, () -> "Could not load " + player.getName() + " on join; retrying in 5s");
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                if (player.isOnline()) handleJoinAsync(player);
            }, 100L);
            return;
        }

        if (data == null) {
            handleFirstJoin(player);
            return;
        }

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        // Targeted updates only: a full-row save here could overwrite a revive or life
        // change written by another server between the read above and this write.
        if (data.getLastSeen() > 0) {
            pauseGracePeriodForOffline(data, uuid, now);
            data.setLastSeen(0L);
            db.setLastSeen(uuid, 0L);
        }

        if (!Objects.equals(data.getUsername(), player.getName())) {
            data.setUsername(player.getName());
            db.setUsername(uuid, player.getName());
        }

        if (data.isDead()) {
            redirectToLimbo(player, data);
        } else {
            // A player who disconnected on the death screen (or was transferred to Limbo)
            // never fired onPlayerRespawn here; clear leftovers so future deaths aren't skipped.
            pendingLimbo.remove(uuid);
            pendingSurvivalRestore.remove(uuid);
            restoreGameModeIfNeeded(player, uuid, data);
        }
    }

    private void pauseGracePeriodForOffline(PlayerData data, UUID uuid, long now) {
        // Pause grace period while offline: extend graceUntil by offline duration
        // Only adjust if grace hasn't already expired before the player went offline
        if (data.getGraceUntil() > 0 && data.getGraceUntil() > data.getLastSeen()) {
            long offlineDuration = now - data.getLastSeen();
            if (offlineDuration > 0) {
                long adjustedGraceUntil = data.getGraceUntil() + offlineDuration;
                data.setGraceUntil(adjustedGraceUntil);
                db.setGraceUntil(uuid, adjustedGraceUntil);
            }
        }
    }

    private void restoreGameModeIfNeeded(Player player, UUID uuid, PlayerData data) {
        final boolean wasPreviouslyDead = data.getLastDeath() > 0;
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) return;
            if (player.getGameMode() != GameMode.SURVIVAL) {
                if (plugin.isDebugMode()) {
                    plugin.debug(player.getName() + " returned alive, restoring to survival.");
                }
                grantReviveCooldown(uuid);
                hybridWindowUsed.remove(uuid);
                setGameModeSilently(player, GameMode.SURVIVAL);
                if (wasPreviouslyDead) {
                    player.sendMessage(MessageUtil.get("revive-success"));
                }
            }
        });
    }

    private void handleFirstJoin(Player player) {
        PlayerData data = PlayerData.createNew(player.getUniqueId(), player.getName(),
                plugin.getDefaultLives(), plugin.getGracePeriodMillis());
        db.savePlayer(data);
        
        if (plugin.isDebugMode()) {
            plugin.debug("Created new player record for " + player.getName());
        }

        if (data.getGraceUntil() > 0) {
            final PlayerData finalData = data;
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    String timeRemaining = finalData.getGraceTimeRemaining(plugin.getGracePeriodMillis());
                    player.sendMessage(MessageUtil.get("death-grace-period",
                            "time_remaining", timeRemaining));
                }
            });
        }
    }

    private void redirectToLimbo(Player player, PlayerData data) {
        String deathMode = effectiveDeathMode();
        plugin.debug(player.getName() + " is dead (mode: " + deathMode + ")");
        // Remaining hybrid window is derived from the persisted death time so a
        // server restart (which clears hybridWindowUsed) cannot grant a fresh window.
        long hybridRemainingMs = data.getLastDeath() > 0
                ? data.getLastDeath() + cachedHybridTimeout * 1000L - System.currentTimeMillis()
                : 0L;

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) return;

            switch (deathMode) {
                case SSoggySouls.MODE_SPECTATOR -> applySpectatorMode(player);
                case SSoggySouls.MODE_HYBRID -> {
                    if (hybridWindowUsed.contains(player.getUniqueId()) || hybridRemainingMs <= 0) {
                        sendDirectToLimbo(player);
                    } else {
                        applyHybridOnJoin(player, player.getUniqueId(), hybridRemainingMs);
                    }
                }
                default -> sendDirectToLimbo(player);
            }
        });
    }

    private void sendDirectToLimbo(Player player) {
        if (plugin.isSingleServerMode()) {
            applySpectatorMode(player);
            return;
        }

        player.sendMessage(MessageUtil.get(MSG_SENT_TO_LIMBO));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                ServerTransferUtil.sendToLimbo(player);
            }
        }, 20L);
    }

    private void applySpectatorMode(Player player) {
        player.sendMessage(MessageUtil.get(MSG_NOW_SPECTATOR));
        setGameModeSilently(player, GameMode.SPECTATOR);
        mainReviveCheckTask.addSpectator(player.getUniqueId());
    }

    /**
     * Changes game mode without it being mistaken for an external (HRM) revive.
     * The marker is cleared right after the call: if no event fired (mode unchanged)
     * or the change was cancelled, a stale marker would otherwise swallow the next
     * genuine SPECTATOR->SURVIVAL revive.
     */
    private void setGameModeSilently(Player player, GameMode mode) {
        UUID uuid = player.getUniqueId();
        expectedGamemodeChanges.add(uuid);
        try {
            player.setGameMode(mode);
        } finally {
            expectedGamemodeChanges.remove(uuid);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player.hasPermission(PERM_BYPASS)) return;

        UUID uuid = player.getUniqueId();
        if (pendingLimbo.contains(uuid) || deathResultPending.contains(uuid)) return;

        // Skip if still in post-revive immunity
        Long cooldownExpiry = reviveCooldowns.get(uuid);
        if (cooldownExpiry != null && System.currentTimeMillis() >= cooldownExpiry) {
            reviveCooldowns.remove(uuid);
            cooldownExpiry = null;
        }
        if (cooldownExpiry != null) {
            if (plugin.isDebugMode()) {
                plugin.debug(player.getName() + " death ignored (revive cooldown active)");
            }
            pendingSurvivalRestore.add(uuid);
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    player.sendMessage(MessageUtil.get("death-cooldown"));
                }
            });
            return;
        }

        // Outcome unknown until the async DB check completes; onPlayerRespawn defers
        // to applyDeathOutcome if the player respawns before then.
        deathResultPending.add(uuid);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> handleDeathAsync(player, uuid));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(PERM_BYPASS)) return;

        UUID uuid = player.getUniqueId();
        mainReviveCheckTask.removeSpectator(uuid);

        // Cancel any pending hybrid transfer since player is offline
        cancelHybridTransfer(uuid);

        long now = System.currentTimeMillis();
        // Run async to avoid blocking the main thread with DB writes
        // Trade-off: may lose very recent quit timestamps on crash, but prevents lag
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
            db.setLastSeen(uuid, now)
        );
    }

    private void handleDeathAsync(Player player, UUID uuid) {
        PlayerData data;
        try {
            data = db.getPlayerStrict(uuid);
        } catch (java.sql.SQLException e) {
            // Retry instead of treating a failed read as "no record": that would save a fresh
            // full-lives record over the real one and drop the death. The death stays pending,
            // so an early respawn keeps waiting for the outcome.
            plugin.getLogger().log(Level.WARNING, e, () -> "Could not load " + player.getName()
                    + " to resolve their death; retrying in 5s");
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> handleDeathAsync(player, uuid), 100L);
            return;
        }
        if (data == null) {
            // Use grace period overload to ensure proper grace tracking for new players
            data = PlayerData.createNew(uuid, player.getName(), plugin.getDefaultLives(),
                                        plugin.getGracePeriodMillis());
            // Save the new player data with grace period set
            db.savePlayer(data);
        }

        if (data.isInGracePeriod(plugin.getGracePeriodMillis())) {
            applyDeathOutcome(player, uuid, false);
            restoreIfAccidentalSpectator(player, uuid);
            notifyGracePeriod(player, data);
            return;
        }

        int remainingLives = data.decrementLife();
        db.savePlayer(data);
        
        if (plugin.isDebugMode()) {
            plugin.debug(player.getName() + " died. Lives remaining: " + remainingLives
                    + ", isDead: " + data.isDead());
        }

        if (data.isDead()) {
            applyDeathOutcome(player, uuid, true);
            handleFinalDeath(player, uuid);
        } else {
            applyDeathOutcome(player, uuid, false);
            restoreIfAccidentalSpectator(player, uuid);
            notifyLifeLost(player, remainingLives);
        }
    }

    /**
     * Publishes the resolved death outcome on the main thread. If the player already
     * respawned while the DB check was running, the matching respawn handling is
     * applied now instead of waiting for a respawn event that already happened.
     */
    private void applyDeathOutcome(Player player, UUID uuid, boolean finalDeath) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            deathResultPending.remove(uuid);
            boolean alreadyRespawned = respawnedBeforeResult.remove(uuid);
            // Head drop is driven by the persisted outcome, not by polling the DB
            org.ssoggy.ssoggysouls.hrm.HeadDropListener headDrops = plugin.getHeadDropListener();
            if (headDrops != null) {
                headDrops.onDeathResolved(player, finalDeath);
            }
            if (finalDeath) {
                if (alreadyRespawned) {
                    handleFinalDeathRespawn(player, uuid);
                } else {
                    pendingLimbo.add(uuid);
                }
            } else if (alreadyRespawned) {
                handleProtectedRespawn(player, uuid);
            } else {
                pendingSurvivalRestore.add(uuid);
            }
        });
    }

    private void restoreIfAccidentalSpectator(Player player, UUID uuid) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline() && player.getGameMode() == GameMode.SPECTATOR) {
                setGameModeSilently(player, GameMode.SURVIVAL);
                cancelHybridTransfer(uuid);
                plugin.debug(player.getName() + " had lives — restored from spectator.");
            }
        });
    }

    private void notifyGracePeriod(Player player, PlayerData data) {
        String timeRemaining = data.getGraceTimeRemaining(plugin.getGracePeriodMillis());
        final String msg = MessageUtil.get("death-grace-period", "time_remaining", timeRemaining);
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                player.sendMessage(msg);
            }
        });
    }

    private void notifyLifeLost(Player player, int remainingLives) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) return;
            if (remainingLives == 1) {
                player.sendMessage(MessageUtil.get("death-last-life"));
            } else {
                player.sendMessage(MessageUtil.get("death-life-lost", "lives", remainingLives));
            }
        });
    }

    private void handleFinalDeath(Player player, UUID uuid) {
        String deathMode = effectiveDeathMode();

        // send death message only, gamemode change sent to onPlayerRespawn
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }

            switch (deathMode) {
                case SSoggySouls.MODE_SPECTATOR ->
                    player.sendMessage(MessageUtil.get(MSG_NOW_SPECTATOR));
                case SSoggySouls.MODE_HYBRID ->
                    player.sendMessage(MessageUtil.get("death-hybrid-warning",
                            "timeout", formatTime(cachedHybridTimeout))); // Use cached value
                default ->
                    player.sendMessage(MessageUtil.get(MSG_SENT_TO_LIMBO));
            }
        });
    }

    private void applyHybridOnJoin(Player player, UUID uuid, long remainingMs) {
        if (plugin.isSingleServerMode()) {
            applySpectatorMode(player);
            return;
        }

        int remainingSeconds = (int) Math.max(1L, Math.min(cachedHybridTimeout, (remainingMs + 999) / 1000));
        hybridWindowUsed.add(uuid);
        player.sendMessage(MessageUtil.get("death-hybrid-warning",
                "timeout", formatTime(remainingSeconds)));
        setGameModeSilently(player, GameMode.SPECTATOR);
        mainReviveCheckTask.addSpectator(uuid);
        scheduleHybridTimeout(player, uuid, remainingSeconds);
    }

    private void scheduleHybridTimeout(Player player, UUID uuid, int timeoutSeconds) {
        if (plugin.isSingleServerMode()) {
            return;
        }

        long delayTicks = timeoutSeconds * 20L;
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            hybridPendingTransfers.remove(uuid);
            if (player.isOnline()) {
                player.sendMessage(MessageUtil.get(MSG_SENT_TO_LIMBO));
                ServerTransferUtil.sendToLimbo(player);
            }
        }, delayTicks);
        // Replaces (and cancels) any previous timer instead of leaking a duplicate
        registerHybridTransfer(uuid, task);
    }

    private static String formatTime(int seconds) {
        if (seconds >= 3600) {
            return (seconds / 3600) + "h " + ((seconds % 3600) / 60) + "m";
        }
        if (seconds >= 60) {
            return (seconds / 60) + "m " + (seconds % 60) + "s";
        }
        return seconds + "s";
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Death outcome still being resolved; applyDeathOutcome finishes the respawn handling
        if (deathResultPending.contains(uuid)) {
            respawnedBeforeResult.add(uuid);
            return;
        }

        // Handle protected deaths (grace period, revive cooldown, or lives remaining)
        // Restore to survival since hardcore mode sets them to spectator on respawn
        if (pendingSurvivalRestore.remove(uuid)) {
            handleProtectedRespawn(player, uuid);
            return;
        }

        // only handle players who died their final actual death
        if (!pendingLimbo.remove(uuid)) return;

        handleFinalDeathRespawn(player, uuid);
    }

    private void handleProtectedRespawn(Player player, UUID uuid) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && player.getGameMode() != GameMode.SURVIVAL) {
                setGameModeSilently(player, GameMode.SURVIVAL);
                cancelHybridTransfer(uuid);
                plugin.debug(player.getName() + " restored to survival after protected death.");
            }
        }, 1L);
    }

    private void handleFinalDeathRespawn(Player player, UUID uuid) {
        String deathMode = effectiveDeathMode();

        // 1 tick delay so client doesn lag behind
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            switch (deathMode) {
                case SSoggySouls.MODE_SPECTATOR -> {
                    setGameModeSilently(player, GameMode.SPECTATOR);
                    mainReviveCheckTask.addSpectator(uuid);
                }
                case SSoggySouls.MODE_HYBRID -> {
                    hybridWindowUsed.add(uuid);
                    setGameModeSilently(player, GameMode.SPECTATOR);
                    mainReviveCheckTask.addSpectator(uuid);
                    scheduleHybridTimeout(player, uuid, cachedHybridTimeout);
                }
                default -> {
                    if (plugin.isSpectatorOnDeath()) {
                        setGameModeSilently(player, GameMode.SPECTATOR);
                        mainReviveCheckTask.addSpectator(uuid);
                    }
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (player.isOnline()) {
                            ServerTransferUtil.sendToLimbo(player);
                        }
                    }, plugin.getSendToLimboDelayTicks());
                }
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        // detect external SPECTATOR->SURVIVAL change (HRM or other plugin revive)
        String deathMode = effectiveDeathMode();
        boolean shouldDetect = !SSoggySouls.MODE_LIMBO.equals(deathMode) || plugin.isDetectHrmRevive();
        if (!shouldDetect) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (expectedGamemodeChanges.remove(uuid)) return;

        if (player.getGameMode() == GameMode.SPECTATOR
                && event.getNewGameMode() == GameMode.SURVIVAL) {

            plugin.debug("Detected gamemode change SPECTATOR->SURVIVAL for "
                    + player.getName() + " (possible HRM revive)");

            // cancel any pending hybrid transfer
            cancelHybridTransfer(uuid);

            grantReviveCooldown(uuid);
            hybridWindowUsed.remove(uuid);

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                PlayerData data = db.getPlayer(uuid);
                if (data != null && data.isDead()) {
                    plugin.getLogger().log(Level.INFO,
                            "HRM revive detected for {0}! Updating database.",
                            player.getName());
                    db.revivePlayer(uuid, plugin.getLivesOnRevive());
                }
            });
        }
    }

    private void grantReviveCooldown(UUID uuid) {
        int seconds = plugin.getReviveCooldownSeconds();
        if (seconds > 0) {
            reviveCooldowns.put(uuid, System.currentTimeMillis() + (seconds * 1000L));
            plugin.debug("Granted " + seconds + "s revive cooldown to " + uuid);
        }
    }

    /**
     * Whether the player's latest death has been resolved as final and they are about to
     * respawn (read by other respawn listeners that run before this one's HIGHEST handler).
     * False while the outcome is still pending.
     */
    public boolean isFinalDeathRespawnPending(UUID uuid) {
        return pendingLimbo.contains(uuid);
    }

    /**
     * cancels any pending hybrid transfer task for the given player.
     *
     * @param uuid the UUID of the player
     */
    public void cancelHybridTransfer(UUID uuid) {
        BukkitTask task = hybridPendingTransfers.remove(uuid);
        if (task != null) {
            task.cancel();
            plugin.debug("Cancelled pending hybrid transfer for " + uuid);
        }
    }

    /**
     * registers a hybrid transfer task for later cancellation or replacement.
     *
     * @param uuid the UUID of the player
     * @param task the scheduled transfer task
     */
    public void registerHybridTransfer(UUID uuid, BukkitTask task) {
        // Cancel any existing task first
        cancelHybridTransfer(uuid);
        hybridPendingTransfers.put(uuid, task);
        plugin.debug("Registered pending hybrid transfer for " + uuid);
    }
}
