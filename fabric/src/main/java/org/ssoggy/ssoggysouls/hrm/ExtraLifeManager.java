package org.ssoggy.ssoggysouls.hrm;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
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

    private static TypedActionResult<ItemStack> handleExtraLifeUse(PlayerEntity player, World world, Hand hand, DatabaseManager db) {
        if (world.isClient || !(player instanceof ServerPlayerEntity serverPlayer)) {
            return TypedActionResult.pass(player.getStackInHand(hand));
        }

        ItemStack stack = player.getStackInHand(hand);
        if (!isExtraLifeItem(stack)) {
            return TypedActionResult.pass(stack);
        }

        // Consume the item immediately to prevent race conditions where
        // the player drops it or uses it multiple times before the async db call
        // completes.
        if (!serverPlayer.isCreative()) {
            stack.decrement(1);
        }

        CompletableFuture.runAsync(() -> processExtraLife(serverPlayer, db));

        return TypedActionResult.consume(stack);
    }

    private static void processExtraLife(ServerPlayerEntity serverPlayer, DatabaseManager db) {
        PlayerData data = getOrCreatePlayerData(serverPlayer, db);

        if (data.isDead()) {
            handleFailedUse(serverPlayer, "extra-life-dead");
            return;
        }

        int maxLives = ConfigManager.getConfig().getMaxLives();
        if (maxLives > 0 && data.getLives() >= maxLives) {
            handleFailedUse(serverPlayer, "extra-life-at-max");
            return;
        }

        grantExtraLife(serverPlayer, db, data.getUuid(), data.getLives());
    }

    private static PlayerData getOrCreatePlayerData(ServerPlayerEntity serverPlayer, DatabaseManager db) {
        PlayerData data = db.getPlayer(serverPlayer.getUuid());
        if (data == null) {
            data = PlayerData.createNew(serverPlayer.getUuid(), serverPlayer.getName().getString(),
                    ConfigManager.getConfig().getDefaultLives(), 0);
            db.savePlayer(data);
        }
        return data;
    }

    private static void handleFailedUse(ServerPlayerEntity serverPlayer, String messageKey) {
        serverPlayer.server.execute(() -> {
            serverPlayer.sendMessage(MessageUtil.get(messageKey), false);
            if (!serverPlayer.isCreative()) {
                ItemStack refundedItem = createExtraLifeItem();
                if (!serverPlayer.getInventory().insertStack(refundedItem)) {
                    serverPlayer.dropItem(refundedItem, false);
                }
            }
        });
    }

    private static void grantExtraLife(ServerPlayerEntity serverPlayer, DatabaseManager db, java.util.UUID uuid, int currentLives) {
        int newLives = currentLives + 1;
        db.setLives(uuid, newLives);

        SSoggySoulsMod.LOGGER.info("{} used Extra Life item (now {} lives)", serverPlayer.getName().getString(),
                newLives);

        serverPlayer.server.execute(() -> {
            serverPlayer.sendMessage(MessageUtil.get("extra-life-gained", "lives", newLives), false);
            serverPlayer.getServerWorld().playSound(null, serverPlayer.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP,
                    SoundCategory.PLAYERS, 1.0f, 1.2f);
            serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 60, 0, false, true));
        });
    }

    public static ItemStack createExtraLifeItem() {
        ItemStack item = new ItemStack(Items.NETHER_STAR);

        item.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Extra Life").styled(s -> s.withColor(Formatting.GREEN).withBold(true)));

        NbtCompound customData = new NbtCompound();
        customData.putBoolean("ExtraLife", true);
        item.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(customData));

        return item;
    }

    private static boolean isExtraLifeItem(ItemStack stack) {
        if (stack.isEmpty() || !stack.contains(DataComponentTypes.CUSTOM_DATA))
            return false;
        NbtComponent nbtComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
        return nbtComponent != null && nbtComponent.contains("ExtraLife");
    }
}
