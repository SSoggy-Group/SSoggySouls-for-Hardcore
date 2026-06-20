package org.ssoggy.ssoggysouls.command;

import java.io.File;
import java.io.IOException;
import java.util.Deque;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.ssoggy.ssoggysouls.SSoggySouls;
import org.ssoggy.ssoggysouls.util.CommandUtil;
import org.ssoggy.ssoggysouls.util.MessageUtil;

public class AdminLogCommand implements CommandExecutor {

    private final SSoggySouls plugin;

    public AdminLogCommand(SSoggySouls plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("java:S3516") // onCommand always returns true by design (Bukkit CommandExecutor convention)
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!isAllowed(sender)) {
            sender.sendMessage(MessageUtil.colorize("&cYou don't have permission to view the admin log."));
            return true;
        }

        int linesToRead = parseLineCount(sender, label, args);
        if (linesToRead < 0) return true; // error already sent

        File logFile = new File(plugin.getDataFolder(), org.ssoggy.ssoggysouls.util.AdminLogger.LOG_FILE_NAME);
        if (!logFile.exists()) {
            sender.sendMessage(MessageUtil.colorize("&eNo admin log file found. No admin actions have been recorded yet."));
            return true;
        }

        readAndDisplayLogAsync(sender, logFile, linesToRead);
        return true;
    }

    private boolean isAllowed(CommandSender sender) {
        if (plugin.isAdminLogAllowAll() || sender.isOp() || sender.hasPermission("ssoggysouls.adminlog")) {
            return true;
        }
        if (sender instanceof org.bukkit.entity.Player player) {
            String uuid = player.getUniqueId().toString();
            String nameLower = player.getName().toLowerCase();
            return plugin.getAdminLogTrustedViewers().contains(uuid)
                    || plugin.getAdminLogTrustedViewers().contains(nameLower);
        }
        return true; // Console always allowed
    }

    /**
     * Parses the optional line count argument. Returns -1 if invalid (error message already sent).
     */
    private int parseLineCount(CommandSender sender, String label, String[] args) {
        if (args.length == 0) return 15;
        try {
            int count = Integer.parseInt(args[0]);
            if (count <= 0 || count > 100) {
                CommandUtil.sendInteractiveUsage(sender, "&cPlease specify a number between 1 and 100.", "/" + label + " ");
                return -1;
            }
            return count;
        } catch (NumberFormatException e) {
            CommandUtil.sendInteractiveUsage(sender, "&cInvalid number: " + args[0], "/" + label + " ");
            return -1;
        }
    }

    private void readAndDisplayLogAsync(CommandSender sender, File logFile, int linesToRead) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            org.ssoggy.ssoggysouls.command.action.AdminLogAction.AdminLogResult result =
                org.ssoggy.ssoggysouls.command.action.AdminLogAction.execute(logFile, linesToRead);

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                switch (result.type) {
                    case FILE_NOT_FOUND -> sender.sendMessage(MessageUtil.colorize("&eNo admin log file found. No admin actions have been recorded yet."));
                    case READ_ERROR -> {
                        plugin.getLogger().log(Level.SEVERE, "Failed to read admin log");
                        sender.sendMessage(MessageUtil.colorize("&cFailed to read admin logs. Check console."));
                    }
                    case SUCCESS -> displayLogEntries(sender, result.lines);
                }
            });
        });
    }

    private void displayLogEntries(CommandSender sender, Deque<String> lastLines) {
        sender.sendMessage(MessageUtil.colorize("&6&l══ Admin Action Log ══"));
        if (lastLines.isEmpty()) {
            sender.sendMessage(MessageUtil.colorize("&7(Empty)"));
        } else {
            for (String line : lastLines) {
                String formattedLine = MessageUtil.colorize(formatLogLine(line));
                if (sender instanceof org.bukkit.entity.Player player) {
                    net.kyori.adventure.text.Component interactiveLine = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(formattedLine)
                            .clickEvent(net.kyori.adventure.text.event.ClickEvent.copyToClipboard(line))
                            .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(net.kyori.adventure.text.Component.text("Click to copy log entry", net.kyori.adventure.text.format.NamedTextColor.GRAY)));
                    player.sendMessage(interactiveLine);
                } else {
                    sender.sendMessage(formattedLine);
                }
            }
        }
        sender.sendMessage(MessageUtil.colorize("&6&l══════════════════════"));
    }

    private static String formatLogLine(String line) {
        if (!line.contains("ADMIN ACTION - ")) {
            return "&7" + line;
        }
        String[] parts = line.split("ADMIN ACTION - ", 2);
        String timestamp = parts[0].replace("[", "&8[").replace("]", "&8]");
        String[] detailParts = parts[1].split(":", 2);
        if (detailParts.length == 2) {
            return timestamp + " &c" + detailParts[0].trim() + " &7- &e" + detailParts[1].trim();
        }
        return "&7" + line;
    }

}
