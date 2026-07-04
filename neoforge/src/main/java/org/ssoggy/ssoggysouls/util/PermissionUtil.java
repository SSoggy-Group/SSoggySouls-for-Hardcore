package org.ssoggy.ssoggysouls.util;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.ChatFormatting;

public final class PermissionUtil {
    private PermissionUtil() {}

    public static boolean isBlockedByLimboOpSecurity(CommandSourceStack source) {
        ConfigManager.ModConfig config = ConfigManager.getConfig();
        if (!config.isLimboOpSecurityCheck()) return false;
        if (!config.isLimboServer()) return false;
        if (!source.isPlayer()) return false;
        if (!source.hasPermission(2)) return false;

        ServerPlayer player = source.getPlayer();
        if (player == null) return false;

        java.util.List<String> trustedAdmins = config.getLimboTrustedAdmins();
        if (!trustedAdmins.isEmpty()) {
            String uuid = player.getUUID().toString();
            String name = player.getScoreboardName().toLowerCase(java.util.Locale.ROOT);
            if (trustedAdmins.contains(uuid) || trustedAdmins.contains(name)) {
                return false;
            }
        }

        return true;
    }

    public static void sendSecurityBlockMessage(CommandSourceStack source) {
        source.sendFailure(Component.literal("Security Error: On the Limbo server, OP status cannot be used to execute this command.").withStyle(ChatFormatting.RED));
        if (source.isPlayer()) {
            net.minecraft.network.chat.MutableComponent message = Component.literal("Either ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("/deop").withStyle(s -> s.withColor(ChatFormatting.YELLOW)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/deop " + source.getTextName()))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to prepare /deop").withStyle(ChatFormatting.GRAY)))))
                    .append(Component.literal(" yourself on Limbo, ask an administrator to add you to the trusted admins list, or have them grant you the bypass permission ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("ssoggysouls.bypass-limbo-op-security").withStyle(s -> s.withColor(ChatFormatting.YELLOW)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "ssoggysouls.bypass-limbo-op-security"))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy permission node").withStyle(ChatFormatting.GRAY)))))
                    .append(Component.literal(".").withStyle(ChatFormatting.GRAY));
            source.sendFailure(message);
        } else {
            source.sendFailure(Component.literal("Either /deop yourself on Limbo, ask an administrator to add you to the trusted admins list, or have them grant you the bypass permission (ssoggysouls.bypass-limbo-op-security).").withStyle(ChatFormatting.GRAY));
        }
    }
}
