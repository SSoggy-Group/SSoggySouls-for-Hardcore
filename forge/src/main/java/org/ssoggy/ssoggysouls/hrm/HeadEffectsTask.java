package org.ssoggy.ssoggysouls.hrm;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
public class HeadEffectsTask {

    private static final int INFINITE_DURATION = -1;
    private static final Set<UUID> wearingHead = new HashSet<>();

    private HeadEffectsTask() {}

    public static void register() {
        // Registration is done automatically via @Mod.EventBusSubscriber
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent event) {
        if (!org.ssoggy.ssoggysouls.util.ConfigManager.getConfig().isHrmEnabled() || !org.ssoggy.ssoggysouls.util.ConfigManager.getConfig().isHeadWearingEffects()) return;

        MinecraftServer server = event.getServer();
        if (server.getTickCount() % 20 != 0) return; // Once per second

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID uuid = player.getUUID();
            boolean wearing = isWearingPlayerHead(player);

            if (wearing && !wearingHead.contains(uuid)) {
                applyEffects(player);
                wearingHead.add(uuid);
            } else if (!wearing && wearingHead.remove(uuid)) {
                removeEffects(player);
            }
        }

        // Cleanup offline players
        wearingHead.removeIf(uuid -> server.getPlayerList().getPlayer(uuid) == null);
    }

    private static boolean isWearingPlayerHead(ServerPlayer player) {
        // Helmet is equipment slot index 3 (head)
        ItemStack helmet = player.getInventory().armor.get(3);
        return !helmet.isEmpty() && helmet.is(Items.PLAYER_HEAD);
    }

    private static void applyEffects(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, INFINITE_DURATION, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, INFINITE_DURATION, 4, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, INFINITE_DURATION, 0, false, false));
    }

    private static void removeEffects(ServerPlayer player) {
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        player.removeEffect(MobEffects.HEALTH_BOOST);
        player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
    }
}
