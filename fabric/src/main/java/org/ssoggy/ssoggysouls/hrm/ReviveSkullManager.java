package org.ssoggy.ssoggysouls.hrm;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.model.PlayerData;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ReviveSkullManager {

    public static void register(SSoggySoulsMod plugin, DatabaseManager db) {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient || !(player instanceof ServerPlayerEntity serverPlayer)) {
                return TypedActionResult.pass(player.getStackInHand(hand));
            }

            ItemStack stack = player.getStackInHand(hand);
            if (!isReviveSkull(stack)) {
                return TypedActionResult.pass(stack);
            }

            // Async DB fetch
            CompletableFuture.runAsync(() -> {
                List<PlayerData> deadPlayers = db.getDeadPlayers();

                serverPlayer.server.execute(() -> {
                    if (deadPlayers.isEmpty()) {
                        serverPlayer.sendMessage(Text.literal("No dead players found.").styled(s -> s.withColor(Formatting.GRAY)), false);
                        return;
                    }
                    openMenu(serverPlayer, deadPlayers);
                });
            });

            return TypedActionResult.consume(stack); // Prevent placing
        });
    }

    private static void openMenu(ServerPlayerEntity player, List<PlayerData> deadPlayers) {
        int rows = Math.min(6, ((deadPlayers.size() - 1) / 9) + 1);
        int slots = rows * 9;
        
        SimpleInventory inventory = new SimpleInventory(slots);
        
        for (int i = 0; i < Math.min(deadPlayers.size(), slots); i++) {
            PlayerData data = deadPlayers.get(i);
            inventory.setStack(i, createMenuHead(data));
        }

        SimpleNamedScreenHandlerFactory factory = new SimpleNamedScreenHandlerFactory((syncId, playerInventory, p) -> {
            ScreenHandlerType<GenericContainerScreenHandler> type = switch(rows) {
                case 1 -> ScreenHandlerType.GENERIC_9X1;
                case 2 -> ScreenHandlerType.GENERIC_9X2;
                case 3 -> ScreenHandlerType.GENERIC_9X3;
                case 4 -> ScreenHandlerType.GENERIC_9X4;
                case 5 -> ScreenHandlerType.GENERIC_9X5;
                default -> ScreenHandlerType.GENERIC_9X6;
            };

            return new GenericContainerScreenHandler(type, syncId, playerInventory, inventory, rows) {
                @Override
                public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity clickingPlayer) {
                    // Cancel all clicks in this inventory to make it read-only
                    if (slotIndex >= 0 && slotIndex < inventory.size()) {
                        ItemStack clicked = inventory.getStack(slotIndex);
                        if (!clicked.isEmpty() && clicked.isOf(Items.PLAYER_HEAD)) {
                            ProfileComponent profile = clicked.get(DataComponentTypes.PROFILE);
                            if (profile != null && profile.id().isPresent()) {
                                UUID ownerId = profile.id().get();
                                String name = profile.name().orElse("Unknown");

                                // Give the real head
                                ItemStack realHead = new ItemStack(Items.PLAYER_HEAD);
                                realHead.set(DataComponentTypes.PROFILE, profile);
                                realHead.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name + "'s Head").styled(s -> s.withColor(Formatting.YELLOW)));
                                
                                clickingPlayer.getInventory().insertStack(realHead);
                                clickingPlayer.sendMessage(Text.literal("Received " + name + "'s head.").styled(s -> s.withColor(Formatting.GREEN)), false);
                                
                                // Schedule closing so we don't crash
                                clickingPlayer.getServer().execute(clickingPlayer::closeHandledScreen);
                            }
                        }
                    } else if (actionType == SlotActionType.QUICK_MOVE || actionType == SlotActionType.SWAP) {
                        return; // Disallow shift clicking into the menu
                    } else {
                        super.onSlotClick(slotIndex, button, actionType, clickingPlayer);
                    }
                }
            };
        }, Text.literal("Revive - Select Player").styled(s -> s.withColor(Formatting.DARK_PURPLE).withBold(true)));

        player.openHandledScreen(factory);
    }

    private static ItemStack createMenuHead(PlayerData data) {
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        head.set(DataComponentTypes.PROFILE, new ProfileComponent(Optional.of(data.getUsername()), Optional.of(data.getUuid()), new com.mojang.authlib.properties.PropertyMap()));
        head.set(DataComponentTypes.CUSTOM_NAME, Text.literal(data.getUsername()).styled(s -> s.withColor(Formatting.RED)));
        return head;
    }

    public static ItemStack createReviveSkullItem() {
        ItemStack item = new ItemStack(Items.PLAYER_HEAD);
        item.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Revive Skull").styled(s -> s.withColor(Formatting.LIGHT_PURPLE).withBold(true)));
        
        NbtCompound customData = new NbtCompound();
        customData.putBoolean("ReviveSkull", true);
        item.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(customData));
        
        return item;
    }

    private static boolean isReviveSkull(ItemStack stack) {
        if (stack.isEmpty() || !stack.has(DataComponentTypes.CUSTOM_DATA)) return false;
        NbtComponent nbtComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
        return nbtComponent != null && nbtComponent.contains("ReviveSkull");
    }
}
