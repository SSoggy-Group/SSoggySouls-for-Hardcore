package org.ssoggy.ssoggysouls.util;

import org.bukkit.command.CommandSender;

public final class CommandUtil {

    private CommandUtil() {
    }

    /**
     * checks whether the sender has a permission, sending the default deny message
     * when missing.
     *
     * @param sender     the command sender
     * @param permission the permission node to check
     * @return true when allowed
     */
    public static boolean checkPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(MessageUtil.colorize("&cYou don't have permission to use this command."));
            return false;
        }
        return true;
    }

    /**
     * checks whether the sender has a permission, sending a custom deny message
     * when missing.
     *
     * @param sender     the command sender
     * @param permission the permission node to check
     * @param message    the message to send when denied
     * @return true when allowed
     */
    public static boolean checkPermission(CommandSender sender, String permission, String message) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(MessageUtil.colorize(message));
            return false;
        }
        return true;
    }

    /**
     * Sends an interactive usage message that can be clicked to auto-fill the command.
     *
     * @param sender     the command sender
     * @param usageText  the usage text to display (e.g., "&cUsage: /cmd <args>")
     * @param suggestCmd the command to suggest when clicked
     */
    public static void sendInteractiveUsage(CommandSender sender, String usageText, String suggestCmd) {
        if (sender instanceof org.bukkit.entity.Player player) {
            net.kyori.adventure.text.Component message = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(usageText)
                    .clickEvent(net.kyori.adventure.text.event.ClickEvent.suggestCommand(suggestCmd))
                    .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(net.kyori.adventure.text.Component.text("Click to auto-fill this command", net.kyori.adventure.text.format.NamedTextColor.GRAY)));
            player.sendMessage(message);
        } else {
            sender.sendMessage(MessageUtil.colorize(usageText));
        }
    }
}
