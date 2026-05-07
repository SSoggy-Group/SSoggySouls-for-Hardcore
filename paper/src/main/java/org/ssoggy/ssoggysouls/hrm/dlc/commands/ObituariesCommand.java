/*
RevivePlus by Cera and Jakeccz
Copyright (C) 2026 Commune

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with RevivePlus.  If not, see <https://www.gnu.org/licenses/>
 */

package org.ssoggy.ssoggysouls.hrm.dlc.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import java.util.List;
import org.ssoggy.ssoggysouls.hrm.dlc.enums.SOCIALENUM;
import org.ssoggy.ssoggysouls.hrm.dlc.enums.COMMANDOUTPUTENUM;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPCommandOutput;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPSocial;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStatic;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPUtil;
import org.ssoggy.ssoggysouls.hrm.dlc.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class ObituariesCommand implements CommandExecutor, TabCompleter {
    private static final long DEFAULT_TRUSTED_OBITUARY_DELAY_MINUTES = 60L;
    private static final long DEFAULT_FRIENDS_OBITUARY_DELAY_MINUTES = 600L;
    private static final long DEFAULT_PUBLIC_OBITUARY_DELAY_MINUTES = 3600L;

    @Override
    public boolean onCommand(@NotNull CommandSender cmdSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(cmdSender instanceof Player)) return true;
        Player player = (Player) cmdSender;

        RPCommandOutput result = new RPCommandOutput();
        FileConfiguration config = RPStatic.CLIENT.getConfig();

        long trustedAfterMin = getObituaryDelayMinutes(config, "trusted-obituary-after", DEFAULT_TRUSTED_OBITUARY_DELAY_MINUTES);
        long friendsAfterMin = getObituaryDelayMinutes(config, "friends-obituary-after", DEFAULT_FRIENDS_OBITUARY_DELAY_MINUTES);
        long publicAfterMin = getObituaryDelayMinutes(config, "public-obituary-after", DEFAULT_PUBLIC_OBITUARY_DELAY_MINUTES);

        String headerText = "Here is a list of all the current public deaths";
        StringBuilder deathListBuilder = new StringBuilder(headerText);
        for (Map.Entry<UUID, Pair<Location, Instant>> death : RPStatic.DEAD_LOCATIONS.entrySet()) {
            Pair<Location, Instant> deathDetails = death.getValue();

            UUID uuid = death.getKey();
            SOCIALENUM relationship = new RPSocial(uuid).getRelationTo(player.getUniqueId());

            Instant deathTime = deathDetails.getRight();
            Instant now = Instant.now();
            if (deathTime.isBefore(now.minusSeconds(publicAfterMin * 60))
                    || (relationship == SOCIALENUM.FRIENDS && deathTime.isBefore(now.minusSeconds(friendsAfterMin * 60)))
                    || (relationship == SOCIALENUM.TRUSTED && deathTime.isBefore(now.minusSeconds(trustedAfterMin * 60)))) {
                String username = RPUtil.getUsernameFromCache(uuid);
                Location deathLocation = deathDetails.getLeft();
                deathListBuilder.append("\n<gold><bold>").append(username).append("</bold></gold><gray> has died at</gray><gold><bold>")
                        .append(" X").append(deathLocation.getBlockX())
                        .append(" Y").append(deathLocation.getBlockY())
                        .append(" Z").append(deathLocation.getBlockZ())
                        .append("</bold></gold><gray> in the </gray><gold><bold>")
                        .append(deathLocation.getWorld().getName()).append("</bold></gold>");
            }
        }

        if (deathListBuilder.length() <= headerText.length()) {
            result.success = COMMANDOUTPUTENUM.FALSE;
            result.message = "There are no public deaths currently.";
        } else {
            result.success = COMMANDOUTPUTENUM.TRUE;
            result.message = deathListBuilder.toString();
        }

        cmdSender.sendRichMessage(result.toString());
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
    }

    private static long getObituaryDelayMinutes(FileConfiguration config, String key, long fallback) {
        String hrmPath = "hrm." + key;
        if (config.contains(hrmPath)) {
            return config.getLong(hrmPath);
        }
        return config.getLong(key, fallback);
    }
}
