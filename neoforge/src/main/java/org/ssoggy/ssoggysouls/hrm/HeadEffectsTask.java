package org.ssoggy.ssoggysouls.hrm;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
public class HeadEffectsTask {

    private static final int INFINITE_DURATION = -1;

    private HeadEffectsTask() {}

    public static void register() {
        // Registered from SSoggySoulsMod on the NeoForge event bus
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        // When disabled keep running so previously applied effects get stripped
        boolean enabled = org.ssoggy.ssoggysouls.util.ConfigManager.getConfig().isHrmEnabled()
                && org.ssoggy.ssoggysouls.util.ConfigManager.getConfig().isHeadWearingEffects();

        MinecraftServer server = event.getServer();
        if (server.getTickCount() % 20 != 0) return; // Once per second

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            // State-based: the effects are infinite and saved with the player, so an
            // in-memory "who is wearing" set lost track of them across relogs/restarts
            // and let players keep the buffs after taking the head off.
            boolean wearing = enabled && isWearingPlayerHead(player);
            boolean hasEffects = hasHeadEffects(player);

            if (wearing && !hasEffects) {
                applyEffects(player);
            } else if (!wearing && hasEffects) {
                removeEffects(player);
            }
        }
    }

    private static boolean isWearingPlayerHead(ServerPlayer player) {
        ItemStack helmet = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
        return !helmet.isEmpty() && helmet.is(Items.PLAYER_HEAD);
    }

    private static void applyEffects(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, INFINITE_DURATION, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, INFINITE_DURATION, HEALTH_BOOST_AMPLIFIER, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, INFINITE_DURATION, 0, false, false));
        player.addTag(OWNER_TAG);
    }

    private static final int HEALTH_BOOST_AMPLIFIER = 4;

    /** Infinite and at the exact amplifier this task applies; anything else is not ours. */
    private static boolean isOurs(MobEffectInstance effect, int amplifier) {
        return effect != null && effect.isInfiniteDuration() && effect.getAmplifier() == amplifier;
    }

    // Persistent ownership marker (saved in the player's NBT "Tags"): effects can't carry
    // a source, so without it an unrelated infinite Slowness/Resistance would look like ours.
    private static final String OWNER_TAG = "ssoggysouls_head_effects";

    private static boolean hasMatchingEffects(ServerPlayer player) {
        return isOurs(player.getEffect(MobEffects.SLOWNESS), 0)
                || isOurs(player.getEffect(MobEffects.HEALTH_BOOST), HEALTH_BOOST_AMPLIFIER)
                || isOurs(player.getEffect(MobEffects.RESISTANCE), 0);
    }

    private static boolean hasHeadEffects(ServerPlayer player) {
        if (player.entityTags().contains(OWNER_TAG)) {
            boolean any = hasMatchingEffects(player);
            if (!any) {
                player.removeTag(OWNER_TAG); // effects already gone (death, milk)
            }
            return any;
        }
        // Legacy (pre-marker) players: infinite Health Boost V is unique to this task
        return isOurs(player.getEffect(MobEffects.HEALTH_BOOST), HEALTH_BOOST_AMPLIFIER);
    }

    private static void removeEffects(ServerPlayer player) {
        // Only reached when we own the effects (marker or legacy signature)
        if (isOurs(player.getEffect(MobEffects.SLOWNESS), 0)) player.removeEffect(MobEffects.SLOWNESS);
        if (isOurs(player.getEffect(MobEffects.HEALTH_BOOST), HEALTH_BOOST_AMPLIFIER)) player.removeEffect(MobEffects.HEALTH_BOOST);
        if (isOurs(player.getEffect(MobEffects.RESISTANCE), 0)) player.removeEffect(MobEffects.RESISTANCE);
        player.removeTag(OWNER_TAG);
    }
}
