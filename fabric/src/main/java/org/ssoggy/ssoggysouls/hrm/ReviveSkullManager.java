package org.ssoggy.ssoggysouls.hrm;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.model.PlayerData;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ReviveSkullManager {

    private ReviveSkullManager() {}

    public static void register(DatabaseManager db) {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.PASS;
            }

            ItemStack stack = player.getItemInHand(hand);
            if (!isReviveSkull(stack)) {
                return InteractionResult.PASS;
            }

            CompletableFuture.runAsync(() -> {
                PlayerData data = db.getPlayer(serverPlayer.getUUID());
                if (!canUseReviveFeatures(serverPlayer, data)) {
                    refreshPlayerDataAsync(serverPlayer, db);
                    return;
                }

                List<PlayerData> deadPlayers = db.getDeadPlayers();
                serverPlayer.level().getServer().execute(() -> openMenu(serverPlayer, deadPlayers, db));
            });

            return InteractionResult.CONSUME; // Prevent placing
        });
    }

    private static void openMenu(ServerPlayer player, List<PlayerData> deadPlayers, DatabaseManager db) {
        if (deadPlayers == null || deadPlayers.isEmpty()) {
            player.sendSystemMessage(Component.literal("No dead players found.").withStyle(net.minecraft.ChatFormatting.GRAY));
            return;
        }

        int rows = Math.min(6, ((deadPlayers.size() - 1) / 9) + 1);
        int numSlots = rows * 9;

        SimpleContainer container = new SimpleContainer(numSlots);
        for (int i = 0; i < numSlots; i++) {
            container.setItem(i, i < deadPlayers.size() ? createMenuHead(deadPlayers.get(i)) : ItemStack.EMPTY);
        }

        MenuType<ChestMenu> menuType = getMenuType(rows);

        player.openMenu(new SimpleMenuProvider(
                (syncId, playerInv, p) -> new ChestMenu(menuType, syncId, playerInv, container, rows) {
                    @Override
                    public boolean stillValid(Player pl) { return true; }

                    @Override
                    public void clicked(int slotIndex, int button, ContainerInput containerInput, Player clickingPlayer) {
                        if (slotIndex >= 0 && slotIndex < numSlots) {
                            ItemStack clicked = this.slots.get(slotIndex).getItem();
                            handleMenuClick(clicked, clickingPlayer, db);
                        }
                    }
                },
                Component.literal("Revive - Select Player").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE, net.minecraft.ChatFormatting.BOLD)
        ));
    }

    @SuppressWarnings("unchecked")
    private static MenuType<ChestMenu> getMenuType(int rows) {
        return switch (rows) {
            case 1 -> (MenuType<ChestMenu>) (MenuType<?>) MenuType.GENERIC_9x1;
            case 2 -> (MenuType<ChestMenu>) (MenuType<?>) MenuType.GENERIC_9x2;
            case 3 -> (MenuType<ChestMenu>) (MenuType<?>) MenuType.GENERIC_9x3;
            case 4 -> (MenuType<ChestMenu>) (MenuType<?>) MenuType.GENERIC_9x4;
            case 5 -> (MenuType<ChestMenu>) (MenuType<?>) MenuType.GENERIC_9x5;
            default -> (MenuType<ChestMenu>) (MenuType<?>) MenuType.GENERIC_9x6;
        };
    }

    private static void handleMenuClick(ItemStack clicked, Player clickingPlayer, DatabaseManager db) {
        if (!clicked.isEmpty() && clicked.is(Items.PLAYER_HEAD)) {
            ResolvableProfile profile = clicked.get(DataComponents.PROFILE);
            if (profile != null && profile.partialProfile().id() != null) {
                java.util.UUID ownerUuid = profile.partialProfile().id();
                String name = profile.name().orElse("Unknown");

                CompletableFuture.runAsync(() -> {
                    PlayerData data = db.getPlayer(ownerUuid);
                    if (data == null || !data.isDead()) {
                        clickingPlayer.sendSystemMessage(Component.literal(name + " is no longer dead!").withStyle(net.minecraft.ChatFormatting.RED));
                        return;
                    }

                    ItemStack realHead = new ItemStack(Items.PLAYER_HEAD);
                    realHead.set(DataComponents.PROFILE, profile);
                    realHead.set(DataComponents.CUSTOM_NAME, Component.literal(name + "'s Head").withStyle(net.minecraft.ChatFormatting.YELLOW));

                    if (!clickingPlayer.getInventory().add(realHead)) {
                        clickingPlayer.drop(realHead, false, net.minecraft.util.Prediction.SERVER_ONLY);
                    }
                    clickingPlayer.sendSystemMessage(Component.literal("Received " + name + "'s head.").withStyle(net.minecraft.ChatFormatting.GREEN));

                    if (clickingPlayer instanceof ServerPlayer spe) {
                        spe.level().getServer().execute(spe::closeContainer);
                    }
                });
            }
        }
    }

    private static boolean canUseReviveFeatures(ServerPlayer player, PlayerData data) {
        if (data == null) {
            return false;
        }

        if (data.isDead()) {
            player.sendSystemMessage(Component.literal("You cannot revive others while dead!").withStyle(net.minecraft.ChatFormatting.RED));
            return false;
        }

        return true;
    }

    private static void refreshPlayerDataAsync(ServerPlayer player, DatabaseManager db) {
        CompletableFuture.runAsync(() -> {
            PlayerData freshData = db.getPlayer(player.getUUID());
            if (freshData != null && !freshData.isDead()) {
                player.sendSystemMessage(Component.literal("Player data reloaded. Try using the skull again.").withStyle(net.minecraft.ChatFormatting.YELLOW));
            }
        });
    }

    private static ItemStack createMenuHead(PlayerData data) {
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        head.set(DataComponents.PROFILE, ResolvableProfile.createResolved(new GameProfile(
                data.getUuid(),
                data.getUsername()
        )));
        head.set(DataComponents.CUSTOM_NAME, Component.literal(data.getUsername()).withStyle(net.minecraft.ChatFormatting.RED));
        return head;
    }

    public static ItemStack createReviveSkullItem() {
        ItemStack item = new ItemStack(Items.PLAYER_HEAD);
        item.set(DataComponents.CUSTOM_NAME, Component.literal("Revive Skull")
                .withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE, net.minecraft.ChatFormatting.BOLD));

        CompoundTag tag = new CompoundTag();
        tag.putBoolean("ReviveSkull", true);
        item.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        return item;
    }

    private static boolean isReviveSkull(ItemStack stack) {
        if (stack.isEmpty() || !stack.has(DataComponents.CUSTOM_DATA)) return false;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().contains("ReviveSkull");
    }
}
