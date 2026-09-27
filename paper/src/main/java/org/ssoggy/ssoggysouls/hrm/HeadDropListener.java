package org.ssoggy.ssoggysouls.hrm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.ssoggy.ssoggysouls.SSoggySouls;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.model.PlayerData;

public class HeadDropListener implements Listener {

    private static final String PERM_BYPASS = "ssoggysouls.bypass";
    private static final int ENTITIES_PER_TICK = 50;
    private static final int CHUNKS_PER_TICK = 10;
    private static final int PLAYERS_PER_TICK = 5;

    private final SSoggySouls plugin;
    private final DatabaseManager db;
    // Tracks locations of skull blocks placed on death so cleanup can remove them
    // directly, even if their chunk is unloaded at revive time.
    private final Map<UUID, List<Location>> headBlockLocations = new ConcurrentHashMap<>();
    // Death location captured at death, dropped once MainServerListener resolves the outcome
    private final Map<UUID, Location> pendingHeadDrops = new ConcurrentHashMap<>();

    public HeadDropListener(SSoggySouls plugin) {
        this.plugin = plugin;
        this.db = plugin.getDatabaseManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player.hasPermission(PERM_BYPASS)) return;

        World world = player.getWorld();
        Location deathLoc = player.getLocation();
        if (deathLoc == null) return;

        sendDeathLocationMessage(player, deathLoc, world);
        if (plugin.isHrmDropHeads()) {
            pendingHeadDrops.put(player.getUniqueId(), reachableHeadLocation(world, deathLoc));
        }
    }

    /**
     * Called by MainServerListener on the main thread once the death outcome is persisted.
     * Replaces the old fixed-attempt DB polling, which could give up before a slow final-death
     * write landed and lose the head permanently.
     */
    public void onDeathResolved(Player player, boolean finalDeath) {
        Location deathLoc = pendingHeadDrops.remove(player.getUniqueId());
        if (!finalDeath || deathLoc == null || deathLoc.getWorld() == null) return;
        placeOrDropHead(player, deathLoc.getWorld(), deathLoc);
    }

    /**
     * A head dropped/placed below the world is destroyed, leaving the player
     * permanently unrevivable. Void deaths move the head to the top block of the
     * column, or to world spawn if the column is empty (e.g. End void).
     */
    private static Location reachableHeadLocation(World world, Location deathLoc) {
        if (deathLoc.getY() >= world.getMinHeight()) {
            return deathLoc;
        }
        Block top = world.getHighestBlockAt(deathLoc.getBlockX(), deathLoc.getBlockZ());
        if (top.getType().isSolid()) {
            return top.getLocation().add(0.5, 1, 0.5);
        }
        return world.getSpawnLocation();
    }

    private void sendDeathLocationMessage(Player player, Location deathLoc, World world) {
        if (plugin.isHrmDeathLocationMsg()) {
            String coords = deathLoc.getBlockX() + " " + deathLoc.getBlockY() + " " + deathLoc.getBlockZ();
            player.sendRichMessage("<gray><italic>You died at <click:copy_to_clipboard:'" + coords + "'>"
                    + "<hover:show_text:'<gray>Click to copy coordinates</gray>'>"
                    + deathLoc.getBlockX() + ", " + deathLoc.getBlockY() + ", " + deathLoc.getBlockZ()
                    + "</hover></click> in " + world.getName() + "</italic></gray>");
        }
    }

    private void placeOrDropHead(Player player, World world, Location deathLoc) {
        if (plugin.isHrmHeadPlaceAsBlock()) {
            placeHeadAsBlock(player, world, deathLoc);
        } else {
            dropHeadItem(world, deathLoc, player);
        }
    }

    private void placeHeadAsBlock(Player player, World world, Location deathLoc) {
        Block block = findSuitableBlock(world, deathLoc);
        if (block != null) {
            block.setType(Material.PLAYER_HEAD, false);
            Skull skull = (Skull) block.getState();
            skull.setOwningPlayer(player);
            skull.update(true, false);
            // Remember this location so cleanup can find it even
            // if the chunk gets unloaded before the player is revived
            headBlockLocations
                    .computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>())
                    .add(block.getLocation());
            if (plugin.isDebugMode()) {
                plugin.debug("Placed " + player.getName() + "'s head block at "
                        + block.getX() + ", " + block.getY() + ", " + block.getZ());
            }
        } else {
            // Fallback so the head is never lost when no block can be placed.
            dropHeadItem(world, deathLoc, player);
            if (plugin.isDebugMode()) {
                plugin.debug("No suitable block found to place " + player.getName()
                        + "'s head; fell back to item drop.");
            }
        }
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        if (!plugin.isHrmDropHeads() || plugin.isHrmHeadPlaceAsBlock() || !plugin.isHrmHeadNoDespawn()) return;
        UUID ownerUuid = getHeadOwnerUuid(event.getEntity().getItemStack());
        if (ownerUuid == null) return;

        // Never block the main thread on the DB: keep the item (age resets), then
        // let it go if the owner turns out to be alive.
        event.setCancelled(true);
        Item itemEntity = event.getEntity();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerData data;
            try {
                data = db.getPlayerStrict(ownerUuid);
            } catch (java.sql.SQLException e) {
                // Read failed: keep the head (age was reset); the next despawn retries the check
                return;
            }
            if (data == null || !data.isDead()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (itemEntity.isValid()) itemEntity.remove();
                });
            }
        });
    }

    private UUID getHeadOwnerUuid(ItemStack stack) {
        if (stack == null || stack.getType() != Material.PLAYER_HEAD) return null;
        if (!(stack.getItemMeta() instanceof SkullMeta skullMeta)) return null;
        OfflinePlayer owner = skullMeta.getOwningPlayer();
        if (owner == null) return null;
        return owner.getUniqueId();
    }

    private void dropHeadItem(World world, Location deathLoc, Player player) {
        ItemStack head = createPlayerHead(player);
        Item item = world.dropItemNaturally(deathLoc, head);
        if (plugin.isHrmHeadFireproof()) {
            item.setInvulnerable(true);
        }
        if (plugin.isDebugMode()) {
            plugin.debug("Dropped " + player.getName() + "'s head at "
                    + deathLoc.getBlockX() + ", " + deathLoc.getBlockY()
                    + ", " + deathLoc.getBlockZ()
                    + (plugin.isHrmHeadFireproof() ? " (fireproof)" : "")
                    + (plugin.isHrmHeadNoDespawn() ? " (no-despawn)" : ""));
        }
    }

    public static ItemStack createPlayerHead(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);
            meta.displayName(Component.text(player.getName() + "'s Head", NamedTextColor.YELLOW));
            meta.lore(List.of(
                    Component.text("A fallen player's head", NamedTextColor.DARK_RED).decorate(TextDecoration.ITALIC),
                    Component.text("Place on a revival structure to revive", NamedTextColor.GRAY)));
            head.setItemMeta(meta);
        }
        return head;
    }

    // Removes every copy of a player's head from the world when they are revived.
    // Called once per revive, never on death.
    //
    // Two-pass design:
    //
    //  Pass 1 – Targeted removal (always O(n) with n = number of placed head blocks)
    //   headBlockLocations stores the Location of every skull block placed at death.
    //   On revive we look up those exact coords, force-load the chunk if necessary,
    //   verify the block still belongs to this player, set it to AIR, then release
    //   the chunk again if we had to load it.  This is instant and chunk-safe.
    //   Limitation: the map is in-memory only.  If the server restarts between death
    //   and revival the entries are lost and pass 2 acts as the safety net.
    //
    //  Pass 2 – Tick-spread fallback scan
    //   Catches anything pass 1 missed: item entities (item-entity mode), item frames,
    //   player/ender-chest inventories, shulker boxes, and stale skull blocks left by
    //   older plugin versions that lacked location tracking.
    //   Work is spread across multiple server ticks to avoid lag spikes:
    //     • Item entities  – 50 per tick
    //     • Item frames    – 50 per tick
    //     • Chunks (block scan, block-mode only) – 10 per tick
    //     • Online players – 5 per tick
    //   The chunk scan phase is skipped entirely when head-place-as-block is false
    //   because in item-entity mode there are no skull blocks to find in chunks.
    public void removeDroppedHeads(UUID ownerUuid) {
        removeTrackedHeadBlocks(ownerUuid);
        startFallbackScan(ownerUuid);
    }

    /**
     * Pass 1: Remove known skull block locations tracked in-memory.
     * Force-loads chunks if needed, verifies ownership, then releases.
     */
    private void removeTrackedHeadBlocks(UUID ownerUuid) {
        List<Location> knownLocations = headBlockLocations.remove(ownerUuid);
        if (knownLocations == null) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Location loc : knownLocations) {
                removeTrackedBlockAt(loc, ownerUuid);
            }
        });
    }

    private void removeTrackedBlockAt(Location loc, UUID ownerUuid) {
        World w = loc.getWorld();
        if (w == null) return;

        boolean wasLoaded = w.isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
        Block b = w.getBlockAt(loc);

        if (b.getType() == Material.PLAYER_HEAD || b.getType() == Material.PLAYER_WALL_HEAD) {
            BlockState state = b.getState();
            if (state instanceof Skull skull) {
                OfflinePlayer owner = skull.getOwningPlayer();
                if (owner != null && owner.getUniqueId().equals(ownerUuid)) {
                    b.setType(Material.AIR);
                    if (plugin.isDebugMode()) {
                        plugin.debug("Removed tracked head block for " + ownerUuid
                                + " at " + b.getX() + ", " + b.getY() + ", " + b.getZ());
                    }
                }
            }
        }

        // Unload the chunk again if we loaded it just for cleanup
        if (!wasLoaded) {
            w.unloadChunkRequest(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
        }
    }

    /**
     * Pass 2: Tick-spread fallback scan using a state-machine BukkitRunnable.
     */
    private void startFallbackScan(UUID ownerUuid) {
        boolean scanChunks = plugin.isHrmHeadPlaceAsBlock();
        new HeadRemovalTask(ownerUuid, scanChunks).runTaskTimer(plugin, 0L, 1L);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Inner class: tick-spread head removal state machine
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * A tick-spread BukkitRunnable that progressively scans all worlds and online players
     * to remove copies of a revived player's head.
     */
    private static class HeadRemovalTask extends BukkitRunnable {

        private enum Phase { ITEM_ENTITIES, ITEM_FRAMES, CHUNKS, PLAYERS, DONE }

        private final UUID ownerUuid;
        private final boolean scanChunksForBlocks;
        private final List<World> worlds;
        private final List<Player> players;
        private final AtomicInteger removedCount = new AtomicInteger(0);

        private Phase phase = Phase.ITEM_ENTITIES;
        private int worldIndex = 0;
        private int playerIndex = 0;
        private int entityIndex = 0;
        private int chunkIndex = 0;

        private List<Item> currentItemEntities = null;
        private List<ItemFrame> currentItemFrames = null;
        private List<Chunk> currentChunks = null;

        HeadRemovalTask(UUID ownerUuid, boolean scanChunksForBlocks) {
            this.ownerUuid = ownerUuid;
            this.scanChunksForBlocks = scanChunksForBlocks;
            this.worlds = new ArrayList<>(Bukkit.getWorlds());
            this.players = new ArrayList<>(Bukkit.getOnlinePlayers());
        }

        @Override
        public void run() {
            switch (phase) {
                case ITEM_ENTITIES -> processItemEntities();
                case ITEM_FRAMES   -> processItemFrames();
                case CHUNKS        -> processChunks();
                case PLAYERS       -> processPlayers();
                default            -> cancel();
            }
        }

        private void processItemEntities() {
            if (worldIndex >= worlds.size()) {
                transitionTo(Phase.ITEM_FRAMES);
                return;
            }
            if (currentItemEntities == null) {
                currentItemEntities = new ArrayList<>(worlds.get(worldIndex).getEntitiesByClass(Item.class));
                entityIndex = 0;
            }
            int processed = 0;
            while (entityIndex < currentItemEntities.size() && processed < ENTITIES_PER_TICK) {
                Item itemEntity = currentItemEntities.get(entityIndex);
                if (itemEntity.isValid() && isOwnedHead(itemEntity.getItemStack(), ownerUuid)) {
                    itemEntity.remove();
                    removedCount.incrementAndGet();
                }
                entityIndex++;
                processed++;
            }
            if (entityIndex >= currentItemEntities.size()) {
                currentItemEntities = null;
                worldIndex++;
            }
        }

        private void processItemFrames() {
            if (worldIndex >= worlds.size()) {
                transitionTo(scanChunksForBlocks ? Phase.CHUNKS : Phase.PLAYERS);
                return;
            }
            if (currentItemFrames == null) {
                currentItemFrames = new ArrayList<>(worlds.get(worldIndex).getEntitiesByClass(ItemFrame.class));
                entityIndex = 0;
            }
            int processed = 0;
            while (entityIndex < currentItemFrames.size() && processed < ENTITIES_PER_TICK) {
                ItemFrame frame = currentItemFrames.get(entityIndex);
                if (frame.isValid() && isOwnedHead(frame.getItem(), ownerUuid)) {
                    frame.setItem(null);
                    removedCount.incrementAndGet();
                }
                entityIndex++;
                processed++;
            }
            if (entityIndex >= currentItemFrames.size()) {
                currentItemFrames = null;
                worldIndex++;
            }
        }

        private void processChunks() {
            if (worldIndex >= worlds.size()) {
                transitionTo(Phase.PLAYERS);
                return;
            }
            if (currentChunks == null) {
                currentChunks = new ArrayList<>(List.of(worlds.get(worldIndex).getLoadedChunks()));
                chunkIndex = 0;
            }
            int processed = 0;
            while (chunkIndex < currentChunks.size() && processed < CHUNKS_PER_TICK) {
                processChunkTileEntities(currentChunks.get(chunkIndex));
                chunkIndex++;
                processed++;
            }
            if (chunkIndex >= currentChunks.size()) {
                currentChunks = null;
                worldIndex++;
            }
        }

        private void processChunkTileEntities(Chunk chunk) {
            if (!chunk.isLoaded()) return;
            for (BlockState state : chunk.getTileEntities()) {
                if (state instanceof InventoryHolder holder) {
                    removedCount.addAndGet(removeFromInventory(holder.getInventory(), ownerUuid));
                }
                if (state instanceof Skull skull) {
                    OfflinePlayer skullOwner = skull.getOwningPlayer();
                    if (skullOwner != null && skullOwner.getUniqueId().equals(ownerUuid)) {
                        skull.getBlock().setType(Material.AIR);
                        removedCount.incrementAndGet();
                    }
                }
            }
        }

        private void processPlayers() {
            int processed = 0;
            while (playerIndex < players.size() && processed < PLAYERS_PER_TICK) {
                Player player = players.get(playerIndex);
                if (player.isOnline()) {
                    removeFromPlayerInventory(player);
                    removedCount.addAndGet(removeFromInventory(player.getEnderChest(), ownerUuid));
                }
                playerIndex++;
                processed++;
            }
            if (playerIndex >= players.size()) {
                finishScan();
            }
        }

        private void removeFromPlayerInventory(Player player) {
            PlayerInventory inv = player.getInventory();
            for (int i = 0; i < inv.getSize(); i++) {
                if (isOwnedHead(inv.getItem(i), ownerUuid)) {
                    inv.setItem(i, null);
                    removedCount.incrementAndGet();
                }
            }
        }

        private void finishScan() {
            int total = removedCount.get();
            if (total > 0) {
                Bukkit.getLogger().log(java.util.logging.Level.INFO, "Removed {0} player head(s) for UUID {1}",
                        new Object[]{total, ownerUuid});
            }
            cancel();
        }

        private void transitionTo(Phase next) {
            phase = next;
            worldIndex = 0;
        }

        private static int removeFromInventory(Inventory inv, UUID targetUuid) {
            int count = 0;
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);
                if (item == null) continue;

                if (isOwnedHead(item, targetUuid)) {
                    inv.setItem(i, null);
                    count++;
                } else if (isShulkerBox(item.getType())) {
                    count += removeFromShulkerItem(inv, i, item, targetUuid);
                }
            }
            return count;
        }

        private static int removeFromShulkerItem(Inventory inv, int slot, ItemStack item, UUID targetUuid) {
            if (!item.hasItemMeta()) return 0;
            if (!(item.getItemMeta() instanceof BlockStateMeta bsm)) return 0;
            BlockState blockState = bsm.getBlockState();
            if (!(blockState instanceof InventoryHolder shulkerHolder)) return 0;

            Inventory shulkerInv = shulkerHolder.getInventory();
            int count = 0;
            boolean changed = false;
            for (int j = 0; j < shulkerInv.getSize(); j++) {
                if (isOwnedHead(shulkerInv.getItem(j), targetUuid)) {
                    shulkerInv.setItem(j, null);
                    changed = true;
                    count++;
                }
            }
            if (changed) {
                bsm.setBlockState(blockState);
                item.setItemMeta(bsm);
                inv.setItem(slot, item);
            }
            return count;
        }

        private static boolean isShulkerBox(Material type) {
            return Tag.SHULKER_BOXES.isTagged(type);
        }

        private static boolean isOwnedHead(ItemStack stack, UUID targetUuid) {
            if (stack == null || stack.getType() != Material.PLAYER_HEAD) return false;
            if (!(stack.getItemMeta() instanceof SkullMeta skullMeta)) return false;
            OfflinePlayer skullOwner = skullMeta.getOwningPlayer();
            return skullOwner != null && skullOwner.getUniqueId().equals(targetUuid);
        }
    }


    private static Block findSuitableBlock(World world, Location loc) {
        int x = loc.getBlockX();
        int startY = loc.getBlockY();
        int z = loc.getBlockZ();
        int maxY = Math.min(startY + 64, world.getMaxHeight() - 1);

        // Preferred: first air block sitting on top of a solid surface, scanning upward.
        // This naturally rises above lava/water pools to the nearest accessible floor/surface.
        for (int y = startY; y <= maxY; y++) {
            Block candidate = world.getBlockAt(x, y, z);
            if (!isAirBlock(candidate)) continue;
            Block below = world.getBlockAt(x, y - 1, z);
            if (below.getType().isSolid()) {
                return candidate;
            }
        }

        // Fallback: any air block going upward (e.g. open cave ceiling, or hovering above lava)
        for (int y = startY; y <= maxY; y++) {
            Block candidate = world.getBlockAt(x, y, z);
            if (isAirBlock(candidate)) {
                return candidate;
            }
        }

        return null;
    }

    private static boolean isAirBlock(Block b) {
        Material t = b.getType();
        return t == Material.AIR || t == Material.CAVE_AIR || t == Material.VOID_AIR;
    }
}
