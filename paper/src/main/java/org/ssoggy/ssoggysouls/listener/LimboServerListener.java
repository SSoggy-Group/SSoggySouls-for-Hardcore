package org.ssoggy.ssoggysouls.listener;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import org.ssoggy.ssoggysouls.SSoggySouls;
import org.ssoggy.ssoggysouls.util.MessageUtil;

public class LimboServerListener implements Listener {

    public static final java.util.Set<java.util.UUID> LIMBO_CACHE = java.util.concurrent.ConcurrentHashMap.newKeySet();

    private static final String PERM_BYPASS = "ssoggysouls.bypass";

    private final SSoggySouls plugin;
    
    // Cache limbo spawn location to avoid repeated lookups
    private Location cachedLimboSpawn;

    public LimboServerListener(SSoggySouls plugin) {
        this.plugin = plugin;
        refreshLimboSpawnCache();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getGameMode() == GameMode.ADVENTURE) {
                LIMBO_CACHE.add(p.getUniqueId());
            }
        }
    }
    
    /**
     * Refreshes the cached limbo spawn location (call on config reload or spawn change).
     */
    public void refreshLimboSpawnCache() {
        this.cachedLimboSpawn = plugin.getLimboSpawn();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission(PERM_BYPASS)) {
            if (plugin.isDebugMode()) {
                plugin.debug(player.getName() + " has bypass, skipping limbo lockdown.");
            }
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean isDead = plugin.getDatabaseManager().isPlayerDead(player.getUniqueId());

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;

                if (isDead) {
                    applyLimboState(player);
                } else {
                    if (plugin.isDebugMode()) {
                        plugin.debug(player.getName() + " is alive, visiting Limbo.");
                    }
                    player.setGameMode(GameMode.SURVIVAL);
                    player.sendMessage(MessageUtil.get("limbo-visitor-welcome"));
                }
            });
        });
    }

    private void applyLimboState(Player player) {
        player.setGameMode(GameMode.ADVENTURE);

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        player.setExp(0);
        player.setLevel(0);

        AttributeInstance maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        double maxHealth = maxHealthAttr != null ? maxHealthAttr.getBaseValue() : 20.0;
        player.setHealth(maxHealth);
        player.setFoodLevel(20);
        player.setSaturation(20f);

        Location spawn = cachedLimboSpawn; // Use cached value
        if (spawn != null && spawn.getWorld() != null) {
            player.teleport(findSafeLocation(spawn));
        } else {
            player.teleport(player.getWorld().getSpawnLocation());
            plugin.getLogger().warning("Limbo spawn not set! Using world spawn. "
                    + "Use /setlimbospawn to configure.");
        }

        player.sendMessage(MessageUtil.getNoPrefix("limbo-welcome"));
        
        LIMBO_CACHE.add(player.getUniqueId());

        if (plugin.isDebugMode()) {
            plugin.debug("Applied limbo state to " + player.getName());
        }
    }

    private static Location findSafeLocation(Location loc) {
        Location safe = loc.clone();
        org.bukkit.World world = safe.getWorld();
        if (world == null) return safe;

        int maxY = world.getMaxHeight();
        int safeBlockX = safe.getBlockX();
        int safeBlockZ = safe.getBlockZ();
        int startY = safe.getBlockY();

        for (int blockY = startY; blockY < maxY - 1; blockY++) {
            if (world.getBlockAt(safeBlockX, blockY, safeBlockZ).getType().isAir()
                    && world.getBlockAt(safeBlockX, blockY + 1, safeBlockZ).getType().isAir()) {
                safe.setY(blockY);
                return safe;
            }
        }
        // Fallback: use original + 2 to be above the block
        safe.setY(startY + 2.0);
        return safe;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(PERM_BYPASS) || player.hasPermission("ssoggysouls.admin")) return;

        String rawMessage = event.getMessage();
        String command = rawMessage.toLowerCase().split(" ")[0];

        if (isWhitelistedCommand(command)) {
            return;
        }

        event.setCancelled(true);
        processLimboCommand(player, rawMessage);
    }

    private void processLimboCommand(Player player, String rawMessage) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean isDead = plugin.getDatabaseManager().isPlayerDead(player.getUniqueId());

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;

                // visitors (not dead in main) are unrestricted
                if (!isDead) {
                    String commandToRun = rawMessage.startsWith("/") ? rawMessage.substring(1) : rawMessage;
                    if (!commandToRun.isBlank()) {
                        player.performCommand(commandToRun);
                    }
                    return;
                }

                player.sendMessage(MessageUtil.get("limbo-cannot-leave"));
            });
        });
    }

    private static boolean isWhitelistedCommand(String command) {
        return "/msg".equals(command) || "/tell".equals(command)
                || "/r".equals(command) || "/reply".equals(command)
                || "/help".equals(command) || "/list".equals(command)
                || "/pstatus".equals(command)
                || "/psadmin".equals(command) || "/psa".equals(command)
                || "/revive".equals(command) || "/psetlives".equals(command);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Location limboSpawn = cachedLimboSpawn;
        if (limboSpawn != null
                && limboSpawn.getWorld() != null
                && player.getWorld().equals(limboSpawn.getWorld())
                && player.getGameMode() == GameMode.ADVENTURE) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(PERM_BYPASS)) {
            return;
        }
        boolean likelyLimboDead = player.getGameMode() == GameMode.ADVENTURE;
        if (likelyLimboDead) {
            event.setCancelled(true);
        }

        Location from = event.getFrom().clone();
        Location to = event.getTo() != null ? event.getTo().clone() : null;
        UUID uuid = player.getUniqueId();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean dead = plugin.getDatabaseManager().isPlayerDead(uuid);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;
                if (!dead) {
                    if (likelyLimboDead && to != null) {
                        player.teleport(to);
                    }
                    return;
                }
                player.teleport(from);
                player.sendMessage(MessageUtil.get("limbo-cannot-leave"));
            });
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Location to = event.getTo();
        if (to == null) {
            return;
        }

        int minHeight = (to.getWorld() != null ? to.getWorld() : player.getWorld()).getMinHeight();
        if (to.getY() < minHeight) {
            Location spawn = cachedLimboSpawn; // Use cached value
            if (spawn != null && spawn.getWorld() != null) {
                player.teleport(spawn);
            } else {
                player.teleport(player.getWorld().getSpawnLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        LIMBO_CACHE.remove(event.getPlayer().getUniqueId());
    }
}
