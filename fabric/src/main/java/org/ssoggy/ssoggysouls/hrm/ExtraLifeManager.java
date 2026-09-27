package org.ssoggy.ssoggysouls.hrm;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.model.PlayerData;
import org.ssoggy.ssoggysouls.util.ConfigManager;
import org.ssoggy.ssoggysouls.util.MessageUtil;

import java.util.concurrent.CompletableFuture;

public class ExtraLifeManager {

    private ExtraLifeManager() {
        // Utility class
    }

    public static void register(DatabaseManager db) {
        UseItemCallback.EVENT.register((player, world, hand) -> handleExtraLifeUse(player, world, hand, db));
    }

    private static InteractionResult handleExtraLifeUse(Player player, Level world, InteractionHand hand, DatabaseManager db) {
        if (world.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (!isExtraLifeItem(stack)) {
            return InteractionResult.PASS;
        }

        if (!serverPlayer.isCreative()) {
            stack.shrink(1);
        }

        CompletableFuture.runAsync(() -> processExtraLife(serverPlayer, db));

        return InteractionResult.CONSUME;
    }

    private static void processExtraLife(ServerPlayer serverPlayer, DatabaseManager db) {
        PlayerData data = getOrCreatePlayerData(serverPlayer, db);
        if (data == null) {
            handleFailedUse(serverPlayer, null); // DB read failed: refund only
            return;
        }

        if (data.isDead()) {
            handleFailedUse(serverPlayer, "extra-life-dead");
            return;
        }

        int maxLives = ConfigManager.getConfig().getMaxLives();
        // Atomic conditional increment: concurrent uses can't overwrite each other's life
        if (!db.incrementLives(data.getUuid(), maxLives)) {
            PlayerData latest = db.getPlayer(data.getUuid());
            handleFailedUse(serverPlayer, latest != null && latest.isDead() ? "extra-life-dead" : "extra-life-at-max");
            return;
        }

        PlayerData updated = db.getPlayer(data.getUuid());
        grantExtraLife(serverPlayer, updated != null ? updated.getLives() : data.getLives() + 1);
    }

    private static PlayerData getOrCreatePlayerData(ServerPlayer serverPlayer, DatabaseManager db) {
        PlayerData data;
        try {
            data = db.getPlayerStrict(serverPlayer.getUUID());
        } catch (java.sql.SQLException e) {
            // Don't create a record over the real one on a failed read
            SSoggySoulsMod.LOGGER.warn("Could not load {} for Extra Life", serverPlayer.getScoreboardName(), e);
            return null;
        }
        if (data == null) {
            data = PlayerData.createNew(serverPlayer.getUUID(), serverPlayer.getScoreboardName(),
                    ConfigManager.getConfig().getDefaultLives(),
                    ConfigManager.parseGracePeriod(ConfigManager.getConfig().getGracePeriod()));
            db.savePlayer(data);
        }
        return data;
    }

    private static void handleFailedUse(ServerPlayer serverPlayer, String messageKey) {
        serverPlayer.level().getServer().execute(() -> {
            if (messageKey != null) {
                serverPlayer.sendSystemMessage(MessageUtil.get(messageKey));
            }
            if (!serverPlayer.isCreative()) {
                ItemStack refundedItem = createExtraLifeItem();
                if (!serverPlayer.getInventory().add(refundedItem)) {
                    serverPlayer.drop(refundedItem, false, net.minecraft.util.Prediction.SERVER_ONLY);
                }
            }
        });
    }

    private static void grantExtraLife(ServerPlayer serverPlayer, int newLives) {

        SSoggySoulsMod.LOGGER.info("{} used Extra Life item (now {} lives)", serverPlayer.getScoreboardName(),
                newLives);

        serverPlayer.level().getServer().execute(() -> {
            serverPlayer.sendSystemMessage(MessageUtil.get("extra-life-gained", "lives", newLives));
            serverPlayer.level().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.2f);
            serverPlayer.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, false, true));
        });
    }

    public static ItemStack createExtraLifeItem() {
        ItemStack item = new ItemStack(Items.NETHER_STAR);
        item.set(DataComponents.CUSTOM_NAME,
                Component.literal("Extra Life").withStyle(net.minecraft.ChatFormatting.GREEN, net.minecraft.ChatFormatting.BOLD));

        CompoundTag tag = new CompoundTag();
        tag.putBoolean("ExtraLife", true);
        item.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        return item;
    }

    public static boolean isExtraLifeItem(ItemStack stack) {
        if (stack.isEmpty() || !stack.has(DataComponents.CUSTOM_DATA))
            return false;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().contains("ExtraLife");
    }
}
