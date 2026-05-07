package org.ssoggy.ssoggysouls.command;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Level;
import java.util.stream.Stream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.ssoggy.ssoggysouls.SSoggySouls;
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

        int linesToRead = parseLineCount(sender, args);
        if (linesToRead < 0) return true; // error already sent

        File logFile = new File(plugin.getDataFolder(), "admin.log");
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
    private int parseLineCount(CommandSender sender, String[] args) {
        if (args.length == 0) return 15;
        try {
            int count = Integer.parseInt(args[0]);
            if (count <= 0 || count > 100) {
                sender.sendMessage(MessageUtil.colorize("&cPlease specify a number between 1 and 100."));
                return -1;
            }
            return count;
        } catch (NumberFormatException e) {
            sender.sendMessage(MessageUtil.colorize("&cInvalid number: " + args[0]));
            return -1;
        }
    }

    private void readAndDisplayLogAsync(CommandSender sender, File logFile, int linesToRead) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Deque<String> lastLines = readLastLines(logFile, linesToRead);
                plugin.getServer().getScheduler().runTask(plugin, () ->
                    displayLogEntries(sender, lastLines)
                );
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to read admin log", e);
                plugin.getServer().getScheduler().runTask(plugin, () ->
                    sender.sendMessage(MessageUtil.colorize("&cFailed to read admin logs. Check console."))
                );
            }
        });
    }

    private void displayLogEntries(CommandSender sender, Deque<String> lastLines) {
        sender.sendMessage(MessageUtil.colorize("&6&l══ Admin Action Log ══"));
        if (lastLines.isEmpty()) {
            sender.sendMessage(MessageUtil.colorize("&7(Empty)"));
        } else {
            for (String line : lastLines) {
                sender.sendMessage(MessageUtil.colorize(formatLogLine(line)));
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

    /**
     * Reads only the last N lines from a file using a streaming approach.
     * This avoids loading the entire file into memory, preventing OOM issues.
     */
    private Deque<String> readLastLines(File file, int maxLines) throws IOException {
        Deque<String> lastLines = new ArrayDeque<>(maxLines);
        try (Stream<String> lines = Files.lines(file.toPath())) {
            lines.forEach(line -> {
                if (lastLines.size() >= maxLines) {
                    lastLines.pollFirst();
                }
                lastLines.addLast(line);
            });
        }
        return lastLines;
    }
}
