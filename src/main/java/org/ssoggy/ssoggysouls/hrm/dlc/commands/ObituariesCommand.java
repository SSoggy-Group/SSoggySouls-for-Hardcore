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
import java.util.Collections;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class ObituariesCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender cmdSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(cmdSender instanceof Player)) return true;
        Player player = (Player) cmdSender;

        RPCommandOutput result = new RPCommandOutput();
        FileConfiguration config = RPStatic.CLIENT.getConfig();

        long trusted_after = config.getLong("trusted-obituary-after");
        long friends_after = config.getLong("friends-obituary-after");
        long public_after = config.getLong("public-obituary-after");

        String start_string = "Here is a list of all the current public deaths";
        StringBuilder public_deaths = new StringBuilder(start_string);
        for (Map.Entry<UUID, Pair<Location, Instant>> death : RPStatic.DEAD_LOCATIONS.entrySet()) {
            Pair<Location, Instant> death_details = death.getValue();

            UUID uuid = death.getKey();
            SOCIALENUM relationship = new RPSocial(uuid).getRelationTo(player.getUniqueId());

            Instant death_time = death_details.getRight();
            Instant now = Instant.now();
            if (death_time.isBefore(now.minusSeconds(public_after * 60))
                    || (relationship == SOCIALENUM.FRIENDS && death_time.isBefore(now.minusSeconds(friends_after * 60)))
                    || (relationship == SOCIALENUM.TRUSTED && death_time.isBefore(now.minusSeconds(trusted_after * 60)))) {
                String username = RPUtil.getUsernameFromCache(uuid);
                Location death_location = death_details.getLeft();
                public_deaths.append("\n<gold><bold>").append(username).append("</bold></gold><gray> has died at</gray><gold><bold>")
                        .append(" X").append(death_location.getBlockX())
                        .append(" Y").append(death_location.getBlockY())
                        .append(" Z").append(death_location.getBlockZ())
                        .append("</bold></gold><gray> in the </gray><gold><bold>")
                        .append(death_location.getWorld().getName()).append("</bold></gold>");
            }
        }

        if (public_deaths.length() <= start_string.length()) {
            result.success = COMMANDOUTPUTENUM.FALSE;
            result.message = "There are no public deaths currently.";
        } else {
            result.success = COMMANDOUTPUTENUM.TRUE;
            result.message = public_deaths.toString();
        }

        cmdSender.sendRichMessage(result.toString());
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
    }
}
