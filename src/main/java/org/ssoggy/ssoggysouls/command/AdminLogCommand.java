package org.ssoggy.ssoggysouls.command;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean allowed = false;

        if (plugin.isAdminLogAllowAll() || sender.isOp() || sender.hasPermission("ssoggysouls.adminlog")) {
            allowed = true;
        } else if (sender instanceof org.bukkit.entity.Player player) {
            String uuid = player.getUniqueId().toString();
            String nameLower = player.getName().toLowerCase();
            if (plugin.getAdminLogTrustedViewers().contains(uuid) || plugin.getAdminLogTrustedViewers().contains(nameLower)) {
                allowed = true;
            }
        } else {
            allowed = true; // Console always allowed
        }

        if (!allowed) {
            sender.sendMessage(MessageUtil.colorize("&cYou don't have permission to view the admin log."));
            return true;
        }

        int linesToRead = 15;
        if (args.length > 0) {
            try {
                linesToRead = Integer.parseInt(args[0]);
                if (linesToRead <= 0 || linesToRead > 100) {
                    sender.sendMessage(MessageUtil.colorize("&cPlease specify a number between 1 and 100."));
                    return false;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(MessageUtil.colorize("&cInvalid number: " + args[0]));
                return false;
            }
        }

        File logFile = new File(plugin.getDataFolder(), "admin_abuse.log");
        if (!logFile.exists()) {
            sender.sendMessage(MessageUtil.colorize("&eNo admin log file found. No admin actions have been recorded yet."));
            return true;
        }

        sender.sendMessage(MessageUtil.colorize("&6&l══ Admin Action Log ══"));
        try {
            List<String> allLines = Files.readAllLines(logFile.toPath());
            if (allLines.isEmpty()) {
                sender.sendMessage(MessageUtil.colorize("&7(Empty)"));
            } else {
                int start = Math.max(0, allLines.size() - linesToRead);
                for (int i = start; i < allLines.size(); i++) {
                    String line = allLines.get(i);
                    // Format output nicely based on [Timestamp] ADMIN ACTION - Sender: Action
                    if (line.contains("ADMIN ACTION - ")) {
                        String[] parts = line.split("ADMIN ACTION - ", 2);
                        String timestamp = parts[0].replace("[", "&8[").replace("]", "&8]");
                        String details = parts[1];
                        String[] detailParts = details.split(":", 2);
                        if(detailParts.length == 2) {
                            String adminName = detailParts[0].trim();
                            String action = detailParts[1].trim();
                            sender.sendMessage(MessageUtil.colorize(timestamp + " &c" + adminName + " &7- &e" + action));
                        } else {
                            sender.sendMessage(MessageUtil.colorize("&7" + line));
                        }
                    } else {
                        sender.sendMessage(MessageUtil.colorize("&7" + line));
                    }
                }
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to read admin log", e);
            sender.sendMessage(MessageUtil.colorize("&cFailed to read admin logs. Check console."));
        }
        sender.sendMessage(MessageUtil.colorize("&6&l══════════════════════"));

        return true;
    }
}
