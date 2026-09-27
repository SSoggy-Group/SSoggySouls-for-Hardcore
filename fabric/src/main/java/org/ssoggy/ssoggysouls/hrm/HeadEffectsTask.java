package org.ssoggy.ssoggysouls.hrm;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HeadEffectsTask {

    private static final int INFINITE_DURATION = -1; // -1 is infinite in modern MC
    private HeadEffectsTask() {
        // Utility class
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 20 != 0) return; // Run once per second
            if (!org.ssoggy.ssoggysouls.util.ConfigManager.getConfig().isHrmEnabled()
                    || !org.ssoggy.ssoggysouls.util.ConfigManager.getConfig().isHeadWearingEffects()) return;

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                // State-based: the effects are infinite and saved with the player, so an
                // in-memory "who is wearing" set lost track of them across relogs/restarts
                // and let players keep the buffs after taking the head off.
                boolean wearing = isWearingPlayerHead(player);
                boolean hasEffects = hasHeadEffects(player);

                if (wearing && !hasEffects) {
                    applyEffects(player);
                } else if (!wearing && hasEffects) {
                    removeEffects(player);
                }
            }
        });
    }

    private static boolean isWearingPlayerHead(ServerPlayer player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        return !helmet.isEmpty() && helmet.is(Items.PLAYER_HEAD);
    }

    private static void applyEffects(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, INFINITE_DURATION, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, INFINITE_DURATION, 4, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, INFINITE_DURATION, 0, false, false));
    }

    /** Our Health Boost is the marker: infinite and at our amplifier. */
    private static boolean hasHeadEffects(ServerPlayer player) {
        MobEffectInstance boost = player.getEffect(MobEffects.HEALTH_BOOST);
        return boost != null && boost.isInfiniteDuration() && boost.getAmplifier() == 4;
    }

    private static void removeEffects(ServerPlayer player) {
        // Only strip the infinite copies we added, not effects from beacons/potions
        for (var effect : java.util.List.of(MobEffects.SLOWNESS, MobEffects.HEALTH_BOOST, MobEffects.RESISTANCE)) {
            MobEffectInstance active = player.getEffect(effect);
            if (active != null && active.isInfiniteDuration()) {
                player.removeEffect(effect);
            }
        }
    }
}
