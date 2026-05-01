package org.ssoggy.ssoggysouls.hrm;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HeadEffectsTask {

    private static final int INFINITE_DURATION = -1; // -1 is infinite in 1.20.5+
    private HeadEffectsTask() {
        // Utility class
    }

    public static void register() {
        final Set<UUID> wearingHead = new HashSet<>();
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 20 != 0) return; // Run once per second

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                UUID uuid = player.getUuid();
                boolean wearing = isWearingPlayerHead(player);

                if (wearing && !wearingHead.contains(uuid)) {
                    applyEffects(player);
                    wearingHead.add(uuid);
                } else if (!wearing && wearingHead.remove(uuid)) {
                    removeEffects(player);
                }
            }

            // Cleanup offline players
            wearingHead.removeIf(uuid -> server.getPlayerManager().getPlayer(uuid) == null);
        });
    }

    private static boolean isWearingPlayerHead(ServerPlayerEntity player) {
        ItemStack helmet = player.getInventory().getArmorStack(3); // 3 is helmet slot
        return !helmet.isEmpty() && helmet.isOf(Items.PLAYER_HEAD);
    }

    private static void applyEffects(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, INFINITE_DURATION, 0, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST, INFINITE_DURATION, 4, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, INFINITE_DURATION, 0, false, false));
    }

    private static void removeEffects(ServerPlayerEntity player) {
        player.removeStatusEffect(StatusEffects.SLOWNESS);
        player.removeStatusEffect(StatusEffects.HEALTH_BOOST);
        player.removeStatusEffect(StatusEffects.RESISTANCE);
    }
}
