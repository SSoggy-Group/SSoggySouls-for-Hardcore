package org.ssoggy.ssoggysouls.hrm;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.ssoggy.ssoggysouls.SSoggySouls;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.model.PlayerData;
import org.ssoggy.ssoggysouls.util.MessageUtil;

public class ExtraLifeManager implements Listener {

    private static final String PDC_KEY_VALUE = "extra_life";

    private final SSoggySouls plugin;
    private final DatabaseManager db;
    private final NamespacedKey extraLifeKey;
    private final NamespacedKey recipeKey;
    // Players with an Extra Life use still being processed; blocks right-click spam dupes
    private final Set<UUID> inFlight = ConcurrentHashMap.newKeySet();

    public ExtraLifeManager(SSoggySouls plugin) {
        this.plugin = plugin;
        this.db = plugin.getDatabaseManager();
        String ns = plugin.getName().toLowerCase(java.util.Locale.ROOT);
        this.extraLifeKey = new NamespacedKey(ns, PDC_KEY_VALUE);
        this.recipeKey = new NamespacedKey(ns, "extra_life_recipe");
    }

    public void registerRecipe() {
        ItemStack result = createExtraLifeItem();

        // check recipe shape and ingredients from config
        var cfg = plugin.getConfig();
        String row1 = cfg.getString("extra-life.recipe.row1", "GEG");
        String row2 = cfg.getString("extra-life.recipe.row2", "ENE");
        String row3 = cfg.getString("extra-life.recipe.row3", "GEG");

        ShapedRecipe recipe = new ShapedRecipe(recipeKey, result);
        recipe.shape(row1, row2, row3);

        var ingredientSection = cfg.getConfigurationSection("extra-life.recipe.ingredients");
        if (ingredientSection != null) {
            for (String key : ingredientSection.getKeys(false)) {
                String materialName = ingredientSection.getString(key, "STONE");
                Material mat = (materialName != null) ? Material.matchMaterial(materialName) : null;
                if (mat != null && key.length() == 1) {
                    recipe.setIngredient(key.charAt(0), mat);
                } else {
                    plugin.getLogger().log(Level.WARNING,
                            "Invalid extra-life recipe ingredient: {0}={1}",
                            new Object[]{key, materialName});
                }
            }
        } else {
            recipe.setIngredient('G', Material.GOLD_BLOCK);
            recipe.setIngredient('E', Material.EMERALD);
            recipe.setIngredient('N', Material.NETHER_STAR);
        }

        Bukkit.addRecipe(recipe);
        plugin.debug("Registered Extra Life crafting recipe.");
    }

    public void unregisterRecipe() {
        Bukkit.removeRecipe(recipeKey);
    }

    public ItemStack createExtraLifeItem() {
        String matName = plugin.getConfig().getString("extra-life.item-material", "NETHER_STAR");
        Material itemMaterial = (matName != null) ? Material.matchMaterial(matName) : null;
        if (itemMaterial == null) {
            itemMaterial = Material.NETHER_STAR;
        }

        ItemStack item = new ItemStack(itemMaterial);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Extra Life", NamedTextColor.GREEN, TextDecoration.BOLD));
            meta.lore(List.of(
                    Component.text("Right-click to gain ", NamedTextColor.GRAY).append(Component.text("+1 Life", NamedTextColor.GREEN)),
                    Component.text("Consumed on use", NamedTextColor.DARK_GRAY).decorate(TextDecoration.ITALIC)));
            meta.getPersistentDataContainer().set(extraLifeKey, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private boolean isExtraLifeItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null
                && meta.getPersistentDataContainer().has(extraLifeKey, PersistentDataType.BYTE);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (!isExtraLifeItem(item)) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        if (!inFlight.add(player.getUniqueId())) return;

        // Take one item up front (main thread) so the same stack can never pay for
        // more than one life; it is refunded if the use is rejected.
        ItemStack single = item.clone();
        single.setAmount(1);
        item.setAmount(item.getAmount() - 1);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean consumed = false;
            try {
                consumed = consumeExtraLife(player);
            } finally {
                final boolean used = consumed;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    inFlight.remove(player.getUniqueId());
                    if (!used && player.isOnline()) {
                        player.getInventory().addItem(single).values()
                                .forEach(left -> player.getWorld().dropItem(player.getLocation(), left));
                    }
                });
            }
        });
    }

    private boolean consumeExtraLife(Player player) {
        PlayerData data;
        try {
            data = db.getPlayerStrict(player.getUniqueId());
        } catch (java.sql.SQLException e) {
            // Don't create a record over the real one on a failed read; the item is refunded
            plugin.getLogger().log(Level.WARNING, e, () -> "Could not load " + player.getName() + " for Extra Life");
            return false;
        }
        if (data == null) {
            data = PlayerData.createNew(player.getUniqueId(), player.getName(),
                    plugin.getDefaultLives(), plugin.getGracePeriodMillis());
            db.savePlayer(data);
        }

        if (data.isDead()) {
            Bukkit.getScheduler().runTask(plugin, () ->
                    player.sendMessage(MessageUtil.get("extra-life-dead")));
            return false;
        }

        int maxLives = plugin.getMaxLives();
        // Atomic conditional increment so concurrent writers can't lose this life
        if (!db.incrementLives(data.getUuid(), maxLives)) {
            PlayerData latest = db.getPlayer(data.getUuid());
            boolean nowDead = latest != null && latest.isDead();
            Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(nowDead
                    ? MessageUtil.get("extra-life-dead")
                    : MessageUtil.get("extra-life-max", "max", maxLives)));
            return false;
        }

        PlayerData updated = db.getPlayer(data.getUuid());
        int newLives = updated != null ? updated.getLives() : data.getLives() + 1;
        plugin.getLogger().log(Level.INFO, "{0} used Extra Life item (now {1} lives)",
                new Object[]{player.getName(), newLives});

        final int finalLives = newLives;
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.sendMessage(MessageUtil.get("extra-life-used",
                    "lives", finalLives));

            Location loc = player.getLocation();
            if (loc != null) {
                player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP,
                        SoundCategory.PLAYERS, 1.0f, 1.2f);
            }
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.GLOWING, 60, 0, false, true));
        });
        return true;
    }
}
