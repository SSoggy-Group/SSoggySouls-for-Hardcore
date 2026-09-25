package org.ssoggy.ssoggysouls.util;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

public final class PermissionUtil extends PermissionHelper {
    private PermissionUtil() {}

    public static boolean isBlockedByLimboOpSecurity(CommandSourceStack source) {
        ConfigManager.ModConfig config = ConfigManager.getConfig();
        if (!config.isLimboOpSecurityCheck()) return false;
        if (!config.isLimboServer()) return false;
        if (!source.isPlayer()) return false;
        if (!source.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) return false;

        ServerPlayer player = source.getPlayer();
        if (player == null) return false;

        if (isTrustedAdmin(player.getUUID().toString(), player.getScoreboardName().toLowerCase(java.util.Locale.ROOT), config.getLimboTrustedAdmins())) {
            return false;
        }

        try {
            if (Permissions.check(source, "ssoggysouls.bypass-limbo-op-security", false)) {
                return false;
            }
        } catch (Throwable t) {
            // Ignore
        }

        return true;
    }

    public static void sendSecurityBlockMessage(CommandSourceStack source) {
        source.sendFailure(Component.literal("Security Error: On the Limbo server, OP status cannot be used to execute this command.").withStyle(ChatFormatting.RED));
        if (source.isPlayer()) {
            MutableComponent message = Component.literal("Either ").withStyle(ChatFormatting.GRAY);
            message.append(Component.literal("/deop").withStyle(style -> style
                    .withColor(ChatFormatting.YELLOW)
                    .withClickEvent(new ClickEvent.SuggestCommand("/deop " + source.getPlayer().getScoreboardName()))
                    .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to prepare /deop").withStyle(ChatFormatting.GRAY)))));
            message.append(Component.literal(" yourself on Limbo, ask an administrator to add you to the whitelist, or have them grant you the bypass permission (").withStyle(ChatFormatting.GRAY));
            message.append(Component.literal("ssoggysouls.bypass-limbo-op-security").withStyle(style -> style
                    .withColor(ChatFormatting.YELLOW)
                    .withClickEvent(new ClickEvent.CopyToClipboard("ssoggysouls.bypass-limbo-op-security"))
                    .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to copy permission node").withStyle(ChatFormatting.GRAY)))));
            message.append(Component.literal(").").withStyle(ChatFormatting.GRAY));
            source.sendFailure(message);
        } else {
            source.sendFailure(Component.literal("Either /deop yourself on Limbo, ask an administrator to add you to the whitelist, or have them grant you the bypass permission (ssoggysouls.bypass-limbo-op-security).").withStyle(ChatFormatting.GRAY));
        }
    }
}
