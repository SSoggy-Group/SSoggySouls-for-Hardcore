package org.ssoggy.ssoggysouls.util;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class PermissionUtil {
    private PermissionUtil() {}

    public static boolean isBlockedByLimboOpSecurity(ServerCommandSource source) {
        ConfigManager.ModConfig config = ConfigManager.getConfig();
        if (!config.isLimboOpSecurityCheck()) return false;
        if (!config.isLimboServer()) return false;
        if (!source.isExecutedByPlayer()) return false;
        if (!source.hasPermissionLevel(2)) return false;

        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return false;

        java.util.List<String> trustedAdmins = config.getLimboTrustedAdmins();
        if (!trustedAdmins.isEmpty()) {
            String uuid = player.getUuid().toString();
            String name = player.getName().getString().toLowerCase(java.util.Locale.ROOT);
            if (trustedAdmins.contains(uuid) || trustedAdmins.contains(name)) {
                return false;
            }
        }

        try {
            if (Permissions.check(source, PermissionConstants.SECURITY_ERROR_SUGGESTION_NODE, false)) {
                return false;
            }
        } catch (Throwable t) {
            // Ignore
        }

        return true;
    }

    public static void sendSecurityBlockMessage(ServerCommandSource source) {
        source.sendError(Text.literal(PermissionConstants.SECURITY_ERROR_HEADER).formatted(Formatting.RED));
        if (source.isExecutedByPlayer() && source.getPlayer() != null) {
            ServerPlayerEntity player = source.getPlayer();
            MutableText message = Text.literal(PermissionConstants.SECURITY_ERROR_SUGGESTION_START).formatted(Formatting.GRAY)
                .append(Text.literal(PermissionConstants.SECURITY_ERROR_SUGGESTION_COMMAND).formatted(Formatting.YELLOW)
                    .styled(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, PermissionConstants.SECURITY_ERROR_SUGGESTION_COMMAND + " " + player.getName().getString()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(PermissionConstants.HOVER_DEOP).formatted(Formatting.GRAY)))))
                .append(Text.literal(PermissionConstants.SECURITY_ERROR_SUGGESTION_MIDDLE).formatted(Formatting.GRAY))
                .append(Text.literal("(" + PermissionConstants.SECURITY_ERROR_SUGGESTION_NODE + ")").formatted(Formatting.YELLOW)
                    .styled(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, PermissionConstants.SECURITY_ERROR_SUGGESTION_NODE))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(PermissionConstants.HOVER_COPY).formatted(Formatting.GRAY)))))
                .append(Text.literal(PermissionConstants.SECURITY_ERROR_SUGGESTION_END).formatted(Formatting.GRAY));
            source.sendError(message);
        } else {
            source.sendError(Text.literal(PermissionConstants.SECURITY_ERROR_FALLBACK).formatted(Formatting.GRAY));
        }
    }
}
