package org.ssoggy.ssoggysouls.util;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
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
            if (Permissions.check(source, "ssoggysouls.bypass-limbo-op-security", false)) {
                return false;
            }
        } catch (Throwable t) {
            // Ignore
        }

        return true;
    }

    public static void sendSecurityBlockMessage(ServerCommandSource source) {
        source.sendError(Text.literal("Security Error: On the Limbo server, OP status cannot be used to execute this command.").formatted(Formatting.RED));
        if (source.isExecutedByPlayer()) {
            net.minecraft.text.MutableText message = Text.literal("Either ").formatted(Formatting.GRAY)
                    .append(Text.literal("/deop").styled(s -> s.withColor(Formatting.YELLOW)
                            .withClickEvent(new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.SUGGEST_COMMAND, "/deop " + source.getName()))
                            .withHoverEvent(new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, Text.literal("Click to prepare /deop").formatted(Formatting.GRAY)))))
                    .append(Text.literal(" yourself on Limbo, ask an administrator to add you to the trusted admins list, or have them grant you the bypass permission ").formatted(Formatting.GRAY))
                    .append(Text.literal("ssoggysouls.bypass-limbo-op-security").styled(s -> s.withColor(Formatting.YELLOW)
                            .withClickEvent(new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.COPY_TO_CLIPBOARD, "ssoggysouls.bypass-limbo-op-security"))
                            .withHoverEvent(new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, Text.literal("Click to copy permission node").formatted(Formatting.GRAY)))))
                    .append(Text.literal(".").formatted(Formatting.GRAY));
            source.sendError(message);
        } else {
            source.sendError(Text.literal("Either /deop yourself on Limbo, ask an administrator to add you to the trusted admins list, or have them grant you the bypass permission (ssoggysouls.bypass-limbo-op-security).").formatted(Formatting.GRAY));
        }
    }
}
