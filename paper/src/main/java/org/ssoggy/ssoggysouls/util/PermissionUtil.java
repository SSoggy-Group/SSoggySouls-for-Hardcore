package org.ssoggy.ssoggysouls.util;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.ssoggy.ssoggysouls.SSoggySouls;

public final class PermissionUtil {

    private PermissionUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * checks whether a sender should be blocked by limbo-only OP security rules.
     *
     * @param sender the command sender to check
     * @param plugin the plugin instance
     * @return true when the sender should be blocked
     */
    public static boolean isBlockedByLimboOpSecurity(CommandSender sender, SSoggySouls plugin) {
        // If security check is disabled, allow all
        if (!plugin.isLimboOpSecurityEnabled()) {
            return false;
        }

        // Only check players, not console or command blocks
        if (!(sender instanceof Player player)) {
            return false;
        }

        // Only apply security check on Limbo server
        if (!plugin.isLimboServer()) {
            return false;
        }

        // Limbo OP security only targets OP players; for non-OP players, this check does not apply
        // and normal permission checks elsewhere will determine whether they can execute the command.
        if (!player.isOp()) {
            return false;
        }

        // Check if player is in the trusted admins list (by UUID or username)
        Set<String> trustedAdmins = plugin.getLimboTrustedAdmins();
        if (!trustedAdmins.isEmpty()) {
            String playerUuid = player.getUniqueId().toString();
            String playerNameLowercase = player.getName().toLowerCase();
            
            // Check both UUID (exact match) and username (case-insensitive via lowercase)
            if (trustedAdmins.contains(playerUuid) || trustedAdmins.contains(playerNameLowercase)) {
                return false;
            }
        }

        // Player is OP on Limbo server - block unless they have bypass permission
        return !player.hasPermission("ssoggysouls.bypass-limbo-op-security");
    }

    /**
     * sends the limbo OP security block message.
     *
     * @param sender the command sender
     */
    public static void sendSecurityBlockMessage(CommandSender sender) {
        sender.sendMessage(MessageUtil.colorize("&cSecurity Error: On the Limbo server, OP status cannot be used to execute this command."));
        if (sender instanceof Player player) {
            net.kyori.adventure.text.Component message = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize("&7Either ")
                    .append(net.kyori.adventure.text.Component.text("/deop", net.kyori.adventure.text.format.NamedTextColor.YELLOW)
                            .clickEvent(net.kyori.adventure.text.event.ClickEvent.suggestCommand("/deop " + player.getName()))
                            .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(net.kyori.adventure.text.Component.text("Click to prepare /deop", net.kyori.adventure.text.format.NamedTextColor.GRAY))))
                    .append(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize("&7 yourself on Limbo, ask an administrator to add you to the trusted admins list, or have them grant you the bypass permission &e("))
                    .append(net.kyori.adventure.text.Component.text("ssoggysouls.bypass-limbo-op-security", net.kyori.adventure.text.format.NamedTextColor.YELLOW)
                            .clickEvent(net.kyori.adventure.text.event.ClickEvent.copyToClipboard("ssoggysouls.bypass-limbo-op-security"))
                            .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(net.kyori.adventure.text.Component.text("Click to copy permission node", net.kyori.adventure.text.format.NamedTextColor.GRAY))))
                    .append(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize("&e)&7."));
            player.sendMessage(message);
        } else {
            sender.sendMessage(MessageUtil.colorize("&7Either /deop yourself on Limbo, ask an administrator to add you to the trusted admins list, or have them grant you the bypass permission &e(ssoggysouls.bypass-limbo-op-security)&7."));
        }
    }
}
