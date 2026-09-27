package org.ssoggy.ssoggysouls.hrm;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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

    public HeadEffectsTask(SSoggySouls plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.isHrmHeadEffects() || !Boolean.TRUE.equals(
                org.ssoggy.ssoggysouls.hrm.dlc.util.RPStatic.CONFIG_RULES == null ? Boolean.TRUE
                        : org.ssoggy.ssoggysouls.hrm.dlc.util.RPStatic.CONFIG_RULES.getOrDefault("head-effects", true))) {
            return;
        }

        // State-based rather than transition-based: the effects are infinite and saved
        // with the player, so an in-memory "who is wearing" set would lose track of them
        // across relogs/restarts and let players keep the buffs without the head.
        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean wearing = isWearingPlayerHead(player);
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

    /** Our Health Boost is the marker: infinite and at our amplifier. */
    private static boolean hasHeadEffects(Player player) {
        PotionEffect boost = player.getPotionEffect(PotionEffectType.HEALTH_BOOST);
        return isOurs(boost) && boost.getAmplifier() == HEALTH_BOOST_AMPLIFIER;
    }

    private static boolean isOurs(PotionEffect effect) {
        // Older versions used Integer.MAX_VALUE instead of a true infinite duration
        return effect != null && (effect.isInfinite() || effect.getDuration() > 1_000_000);
    }

    private static void applyEffects(Player player) {
        // Use cached potion effects instead of creating new instances
        player.addPotionEffect(NAUSEA_EFFECT);
        player.addPotionEffect(SLOWNESS_EFFECT);
        player.addPotionEffect(HEALTH_BOOST_EFFECT);
        player.addPotionEffect(RESISTANCE_EFFECT);
    }

    private static void removeEffects(Player player) {
        // Only strip the infinite copies we added, not effects from beacons/potions
        for (PotionEffectType type : new PotionEffectType[] {
                PotionEffectType.SLOWNESS, PotionEffectType.HEALTH_BOOST, PotionEffectType.RESISTANCE}) {
            if (isOurs(player.getPotionEffect(type))) {
                player.removePotionEffect(type);
            }
        }
    }
}
