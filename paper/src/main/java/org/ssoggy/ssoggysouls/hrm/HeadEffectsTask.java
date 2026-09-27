package org.ssoggy.ssoggysouls.hrm;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import org.ssoggy.ssoggysouls.SSoggySouls;

// does that thingy with effects when you wear player head
public class HeadEffectsTask extends BukkitRunnable {

    private static final int INFINITE_DURATION = PotionEffect.INFINITE_DURATION;
    private static final int HEALTH_BOOST_AMPLIFIER = 4;

    // Cache potion effects to avoid creating new instances every time
    private static final PotionEffect NAUSEA_EFFECT = new PotionEffect(
            PotionEffectType.NAUSEA, 200, 0, false, false);
    private static final PotionEffect SLOWNESS_EFFECT = new PotionEffect(
            PotionEffectType.SLOWNESS, INFINITE_DURATION, 0, false, false);
    private static final PotionEffect HEALTH_BOOST_EFFECT = new PotionEffect(
            PotionEffectType.HEALTH_BOOST, INFINITE_DURATION, HEALTH_BOOST_AMPLIFIER, false, false);
    private static final PotionEffect RESISTANCE_EFFECT = new PotionEffect(
            PotionEffectType.RESISTANCE, INFINITE_DURATION, 0, false, false);

    private final SSoggySouls plugin;
    private final NamespacedKey ownerKey;

    public HeadEffectsTask(SSoggySouls plugin) {
        this.plugin = plugin;
        this.ownerKey = new NamespacedKey(plugin, "head_effects");
    }

    @Override
    public void run() {
        // When disabled keep running so previously applied effects get stripped
        boolean enabled = plugin.isHrmHeadEffects() && Boolean.TRUE.equals(
                org.ssoggy.ssoggysouls.hrm.dlc.util.RPStatic.CONFIG_RULES == null ? Boolean.TRUE
                        : org.ssoggy.ssoggysouls.hrm.dlc.util.RPStatic.CONFIG_RULES.getOrDefault("head-effects", true));

        // State-based rather than transition-based: the effects are infinite and saved
        // with the player, so an in-memory "who is wearing" set would lose track of them
        // across relogs/restarts and let players keep the buffs without the head.
        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean wearing = enabled && isWearingPlayerHead(player);
            boolean hasEffects = hasHeadEffects(player);

            if (wearing && !hasEffects) {
                applyEffects(player);
                if (plugin.isDebugMode()) {
                    plugin.debug(player.getName() + " equipped a player head, applying effects.");
                }
            } else if (!wearing && hasEffects) {
                removeEffects(player);
                if (plugin.isDebugMode()) {
                    plugin.debug(player.getName() + " removed player head, removing effects.");
                }
            }
        }
    }

    private static boolean isWearingPlayerHead(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        return helmet != null && helmet.getType() == Material.PLAYER_HEAD;
    }

    private static boolean hasMatchingEffects(Player player) {
        return isOurs(player.getPotionEffect(PotionEffectType.SLOWNESS), 0)
                || isOurs(player.getPotionEffect(PotionEffectType.HEALTH_BOOST), HEALTH_BOOST_AMPLIFIER)
                || isOurs(player.getPotionEffect(PotionEffectType.RESISTANCE), 0);
    }

    private boolean hasHeadEffects(Player player) {
        // Persistent ownership marker: effects can't carry a source, so without it an
        // unrelated infinite Slowness/Resistance would look like ours.
        if (player.getPersistentDataContainer().has(ownerKey, PersistentDataType.BYTE)) {
            boolean any = hasMatchingEffects(player);
            if (!any) {
                player.getPersistentDataContainer().remove(ownerKey); // effects already gone (death, milk)
            }
            return any;
        }
        // Legacy (pre-marker) players: an infinite Health Boost V is unique to this task
        return isOurs(player.getPotionEffect(PotionEffectType.HEALTH_BOOST), HEALTH_BOOST_AMPLIFIER);
    }

    /** Infinite and at the exact amplifier this task applies; anything else is not ours. */
    private static boolean isOurs(PotionEffect effect, int amplifier) {
        // Older versions used Integer.MAX_VALUE instead of a true infinite duration
        return effect != null && effect.getAmplifier() == amplifier
                && (effect.isInfinite() || effect.getDuration() > 1_000_000);
    }

    private void applyEffects(Player player) {
        // Use cached potion effects instead of creating new instances
        player.addPotionEffect(NAUSEA_EFFECT);
        player.addPotionEffect(SLOWNESS_EFFECT);
        player.addPotionEffect(HEALTH_BOOST_EFFECT);
        player.addPotionEffect(RESISTANCE_EFFECT);
        player.getPersistentDataContainer().set(ownerKey, PersistentDataType.BYTE, (byte) 1);
    }

    private void removeEffects(Player player) {
        // Only reached when we own the effects (marker or legacy signature)
        if (isOurs(player.getPotionEffect(PotionEffectType.SLOWNESS), 0)) {
            player.removePotionEffect(PotionEffectType.SLOWNESS);
        }
        if (isOurs(player.getPotionEffect(PotionEffectType.HEALTH_BOOST), HEALTH_BOOST_AMPLIFIER)) {
            player.removePotionEffect(PotionEffectType.HEALTH_BOOST);
        }
        if (isOurs(player.getPotionEffect(PotionEffectType.RESISTANCE), 0)) {
            player.removePotionEffect(PotionEffectType.RESISTANCE);
        }
        player.getPersistentDataContainer().remove(ownerKey);
    }
}
