package org.ssoggy.ssoggysouls.hrm;

import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.model.PlayerData;
import org.ssoggy.ssoggysouls.util.ConfigManager;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
public class ReviveSkullManager {

    private static DatabaseManager db;

    private ReviveSkullManager() {}

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
        if (!isReviveSkull(stack)) {
            return;
        }

        event.setCanceled(true);

        CompletableFuture.runAsync(() -> {
            List<PlayerData> deadPlayers = db.getDeadPlayers();

            serverPlayer.server.execute(() -> {
                if (deadPlayers.isEmpty()) {
                    serverPlayer.sendSystemMessage(Component.literal("No dead players found.").withStyle(net.minecraft.ChatFormatting.GRAY));
                    return;
                }
                openMenu(serverPlayer, deadPlayers);
            });
        });
    }

    private static void openMenu(ServerPlayer player, List<PlayerData> deadPlayers) {
        int rows = Math.min(6, ((deadPlayers.size() - 1) / 9) + 1);
        int numSlots = rows * 9;

        // Build a SimpleContainer populated with player heads
        net.minecraft.world.SimpleContainer container = new net.minecraft.world.SimpleContainer(numSlots);
        for (int i = 0; i < numSlots; i++) {
            container.setItem(i, i < deadPlayers.size() ? createMenuHead(deadPlayers.get(i)) : ItemStack.EMPTY);
        }

        MenuType<ChestMenu> menuType = getMenuType(rows);

        player.openMenu(new SimpleMenuProvider((syncId, playerInv, p) ->
                new ChestMenu(menuType, syncId, playerInv, container, rows) {
                    @Override
                    public boolean stillValid(Player pl) { return true; }

                    @Override
                    public void clicked(int slotIndex, int button, ClickType clickType, Player clickingPlayer) {
                        if (slotIndex >= 0 && slotIndex < numSlots) {
                            ItemStack clicked = this.slots.get(slotIndex).getItem();
                            handleMenuClick(clicked, clickingPlayer);
                        }
                        // Block all regular inventory interaction to prevent item theft
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

    private static void handleMenuClick(ItemStack clicked, Player clickingPlayer) {
        if (!clicked.isEmpty() && clicked.is(Items.PLAYER_HEAD)) {
            ResolvableProfile profile = clicked.get(DataComponents.PROFILE);
            if (profile != null && profile.id().isPresent()) {
                String name = profile.name().orElse("Unknown");

                ItemStack realHead = new ItemStack(Items.PLAYER_HEAD);
                realHead.set(DataComponents.PROFILE, profile);
                realHead.set(DataComponents.CUSTOM_NAME, Component.literal(name + "'s Head").withStyle(net.minecraft.ChatFormatting.YELLOW));

                if (!clickingPlayer.getInventory().add(realHead)) {
                    clickingPlayer.drop(realHead, false);
                }
                clickingPlayer.sendSystemMessage(Component.literal("Received " + name + "'s head.").withStyle(net.minecraft.ChatFormatting.GREEN));

                if (clickingPlayer instanceof ServerPlayer spe) {
                    spe.getServer().execute(spe::closeContainer);
                }
            }
        }
    }

    private static ItemStack createMenuHead(PlayerData data) {
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        head.set(DataComponents.PROFILE, new ResolvableProfile(
                Optional.of(data.getUsername()),
                Optional.of(data.getUuid()),
                new PropertyMap()
        ));
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

    public static boolean isReviveSkull(ItemStack stack) {
        if (stack.isEmpty() || !stack.has(DataComponents.CUSTOM_DATA)) return false;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.contains("ReviveSkull");
    }
}
