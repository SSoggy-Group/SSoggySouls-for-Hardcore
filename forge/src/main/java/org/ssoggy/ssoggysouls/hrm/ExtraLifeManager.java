package org.ssoggy.ssoggysouls.hrm;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.model.PlayerData;
import org.ssoggy.ssoggysouls.util.ConfigManager;
import org.ssoggy.ssoggysouls.util.MessageUtil;

import java.util.concurrent.CompletableFuture;
public class ExtraLifeManager {

    private static DatabaseManager db;

    private ExtraLifeManager() {}

    public static void register(DatabaseManager database) {
        db = database;
    }

    @SubscribeEvent
    public static void onItemRightClick(PlayerInteractEvent.RightClickItem event) {
        ServerPlayer serverPlayer = org.ssoggy.ssoggysouls.util.HrmUtil.getValidServerPlayer(event, db);
        if (serverPlayer == null) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (!isExtraLifeItem(stack)) {
            return;
        }

        event.setCanceled(true);

        if (!serverPlayer.isCreative()) {
            stack.shrink(1);
        }

        CompletableFuture.runAsync(() -> processExtraLife(serverPlayer));
    }

    private static void processExtraLife(ServerPlayer serverPlayer) {
        PlayerData data = getOrCreatePlayerData(serverPlayer);

        if (data.isDead()) {
            handleFailedUse(serverPlayer, "extra-life-dead");
            return;
        }

        int maxLives = ConfigManager.getConfig().getMaxLives();
        if (maxLives > 0 && data.getLives() >= maxLives) {
            handleFailedUse(serverPlayer, "extra-life-at-max");
            return;
        }

        grantExtraLife(serverPlayer, data.getUuid(), data.getLives());
    }

    private static PlayerData getOrCreatePlayerData(ServerPlayer serverPlayer) {
        PlayerData data = db.getPlayer(serverPlayer.getUUID());
        if (data == null) {
            data = PlayerData.createNew(serverPlayer.getUUID(), serverPlayer.getScoreboardName(),
                    ConfigManager.getConfig().getDefaultLives(), 0);
            db.savePlayer(data);
        }
        return data;
    }

    private static void handleFailedUse(ServerPlayer serverPlayer, String messageKey) {
        serverPlayer.server.execute(() -> {
            serverPlayer.sendSystemMessage(MessageUtil.get(messageKey));
            if (!serverPlayer.isCreative()) {
                ItemStack refundedItem = createExtraLifeItem();
                if (!serverPlayer.getInventory().add(refundedItem)) {
                    serverPlayer.drop(refundedItem, false);
                }
            }
        });
    }

    private static void grantExtraLife(ServerPlayer serverPlayer, java.util.UUID uuid, int currentLives) {
        int newLives = currentLives + 1;
        db.setLives(uuid, newLives);

        SSoggySoulsMod.LOGGER.info("{} used Extra Life item (now {} lives)", serverPlayer.getScoreboardName(), newLives);

        serverPlayer.server.execute(() -> {
            serverPlayer.sendSystemMessage(MessageUtil.get("extra-life-gained", "lives", newLives));
            serverPlayer.level().playSound(null, serverPlayer.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                    SoundSource.PLAYERS, 1.0f, 1.2f);
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
        if (stack.isEmpty() || !stack.has(DataComponents.CUSTOM_DATA)) return false;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.contains("ExtraLife");
    }
}
