package org.ssoggy.ssoggysouls.hrm;

import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.ssoggy.ssoggysouls.SSoggySouls;
import org.ssoggy.ssoggysouls.model.PlayerData;
import org.ssoggy.ssoggysouls.util.MessageUtil;

// craftable revive skull thingy from HRM
public class ReviveSkullManager implements Listener {

    private static final Component MENU_TITLE = Component.text("Revive - Select Player", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD);

    private final SSoggySouls plugin;
    private final NamespacedKey reviveSkullKey;

    public ReviveSkullManager(SSoggySouls plugin) {
        this.plugin = plugin;
        // use a stable namespace based on the plugin name to avoid legacy recipe keys because I feel like it
        String ns = plugin.getName().toLowerCase(Locale.ROOT);
        this.reviveSkullKey = new NamespacedKey(ns, "revive_skull");
    }

    public void registerRecipe() {
        ItemStack result = createReviveSkullItem();
        ShapedRecipe recipe = new ShapedRecipe(reviveSkullKey, result);
        recipe.shape("OGO", "TST", "OGO");
        recipe.setIngredient('O', Material.OBSIDIAN);
        recipe.setIngredient('G', Material.GHAST_TEAR);
        recipe.setIngredient('T', Material.TOTEM_OF_UNDYING);
        recipe.setIngredient('S', new RecipeChoice.MaterialChoice(
                Material.SKELETON_SKULL, Material.WITHER_SKELETON_SKULL,
                Material.ZOMBIE_HEAD, Material.CREEPER_HEAD,
                Material.PIGLIN_HEAD, Material.DRAGON_HEAD, Material.PLAYER_HEAD));

        // in case old recipe from a previous plugin exists, remove it
        try {
            NamespacedKey oldKey = new NamespacedKey("hardcorelimbo", "revive_skull");
            Bukkit.removeRecipe(oldKey);
        } catch (Exception ignored) {
            // legacy recipe may not exist so ignore any exception
        }

        Bukkit.addRecipe(recipe);
        plugin.debug("Registered Revive Skull crafting recipe.");
    }

    public void unregisterRecipe() {
        Bukkit.removeRecipe(reviveSkullKey);
    }

    public ItemStack createReviveSkullItem() {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Revive Skull", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD));
            meta.lore(List.of(
                    Component.text("A mysterious skull imbued with revival power", NamedTextColor.DARK_RED)
                            .decorate(TextDecoration.ITALIC),
                    Component.empty(),
                    Component.text("Right-click to select a dead player's head", NamedTextColor.GRAY)));
            meta.getPersistentDataContainer().set(
                    reviveSkullKey, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isReviveSkull(ItemStack item) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(reviveSkullKey, PersistentDataType.BYTE);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().discoverRecipe(reviveSkullKey);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (!isReviveSkull(item)) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<PlayerData> deadPlayers = plugin.getDatabaseManager().getDeadPlayers();

            if (deadPlayers.isEmpty()) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        player.sendMessage(MessageUtil.colorize(
                                "\u00a77No dead players found.")));
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> openMenu(player, deadPlayers));
        });
    }

    private void openMenu(Player player, List<PlayerData> deadPlayers) {
        int slots = Math.min(54, ((deadPlayers.size() / 9) + 1) * 9);
        slots = Math.max(9, slots);
        Inventory inv = Bukkit.createInventory(null, slots, MENU_TITLE);

        int maxItems = Math.min(deadPlayers.size(), slots);
        for (int i = 0; i < maxItems; i++) {
            PlayerData data = deadPlayers.get(i);
            inv.setItem(i, createMenuHead(data));
        }

        player.openInventory(inv);
    }

    private ItemStack createMenuHead(PlayerData data) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(data.getUuid()));
            meta.displayName(Component.text(data.getUsername(), NamedTextColor.RED));
            meta.lore(List.of(
                    Component.text("Status: ", NamedTextColor.GRAY).append(Component.text("Dead", NamedTextColor.DARK_RED)),
                    Component.empty(),
                    Component.text("Click to receive their head", NamedTextColor.YELLOW)));
            head.setItemMeta(meta);
        }
        return head;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!MENU_TITLE.equals(event.getView().title())) return;
        event.setCancelled(true);

        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() != Material.PLAYER_HEAD) return;
        if (!(clicked.getItemMeta() instanceof SkullMeta skullMeta)) return;

        OfflinePlayer owner = skullMeta.getOwningPlayer();
        if (owner == null) return;

        Player player = (Player) event.getWhoClicked();

        ItemStack headItem = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) headItem.getItemMeta();
        if (headMeta != null) {
            headMeta.setOwningPlayer(owner);
            headMeta.displayName(Component.text(owner.getName() + "'s Head", NamedTextColor.YELLOW));
            headMeta.lore(List.of(
                    Component.text("A fallen player's head", NamedTextColor.DARK_RED)
                            .decorate(TextDecoration.ITALIC),
                    Component.text("Place on a revival structure to revive", NamedTextColor.GRAY)));
            headItem.setItemMeta(headMeta);
        }

        player.getInventory().addItem(headItem);
        player.closeInventory();
        player.sendMessage(MessageUtil.colorize(
                "\u0026aReceived \u0026e" + owner.getName() + "\u0026a's head."));
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (MENU_TITLE.equals(event.getView().title())) {
            event.setCancelled(true);
        }
    }

    public NamespacedKey getRecipeKey() {
        return reviveSkullKey;
    }
}
