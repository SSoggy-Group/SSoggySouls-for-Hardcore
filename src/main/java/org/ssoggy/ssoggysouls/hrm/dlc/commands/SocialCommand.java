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
import org.ssoggy.ssoggysouls.hrm.dlc.enums.SOCIALENUM;
import org.ssoggy.ssoggysouls.hrm.dlc.enums.TRUSTENUM;
import org.ssoggy.ssoggysouls.hrm.dlc.enums.COMMANDOUTPUTENUM;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPCommandOutput;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPSocial;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class SocialCommand implements CommandExecutor, TabCompleter {

    public void executeFail(CommandSender cmdSender, RPCommandOutput cmdOutput) {
        cmdOutput.success = COMMANDOUTPUTENUM.FALSE;
        cmdOutput.message = "Please use /trust <action> [player]";
        cmdSender.sendRichMessage(cmdOutput.toString());
    }

    public void executeFail(CommandSender cmdSender, RPCommandOutput cmdOutput, String msg) {
        cmdOutput.success = COMMANDOUTPUTENUM.FALSE;
        cmdOutput.message = msg;
        cmdSender.sendRichMessage(cmdOutput.toString());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender cmdSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        boolean failArgs = args.length < 2;
        RPCommandOutput output = new RPCommandOutput();
        if (failArgs && !(args.length == 1 && args[0].toLowerCase(Locale.ROOT).equals("info"))) {
            executeFail(cmdSender, output);
            return true;
        }

        Entity player = cmdSender instanceof Entity entity ? entity : null;
        OfflinePlayer targetPlayer = failArgs ? null : cmdSender.getServer().getOfflinePlayer(args[1].toLowerCase(Locale.ROOT).trim());
        if (!(player instanceof Player)) return true;
        UUID playerUUID = player.getUniqueId();
        UUID targetPlayerUUID = failArgs ? null : targetPlayer.getUniqueId();
        RPUtil.addUsernameToCache(playerUUID);
        if (targetPlayerUUID == null) return true;

        RPUtil.addUsernameToCache(targetPlayerUUID);
        RPSocial social = new RPSocial(playerUUID);
        output.success = COMMANDOUTPUTENUM.TRUE;
        // TODO: I guess make this respond nicer, maybe extract all the branching logic inside of a method `tryRelationship` inside like `/action/SocialHelper.java`
        try {
            TRUSTENUM action = TRUSTENUM.valueOf(args[0].toUpperCase(Locale.ROOT).trim());
            RPSocial targetSocial = new RPSocial(targetPlayerUUID);
            SOCIALENUM currentRelation = social.getRelationTo(targetPlayerUUID);
            SOCIALENUM theirRelation = targetSocial.getRelationTo(playerUUID);

            switch (action) {
                case TRUSTENUM.BLOCK:
                    if (playerUUID.equals(targetPlayerUUID)) {
                        executeFail(cmdSender, output, "Player has you blocked");
                        return true;
                    }
                    if (currentRelation == SOCIALENUM.BLOCKED) {
                        output.success = COMMANDOUTPUTENUM.INFO;
                        output.message = "You already blocked " + targetPlayer.getName(); // easter egg: You tryna double-block this dude?
                    } else {
                        social.setRelationTo(targetPlayerUUID, SOCIALENUM.BLOCKED);
                        if (theirRelation.isTrustworthy()) { targetSocial.setRelationTo(playerUUID, SOCIALENUM.UNTRUSTED); } // Unbind both players
                        output.message = "You have blocked " + targetPlayer.getName();
                    }
                    break;
                case TRUSTENUM.REVOKE:
                    if (currentRelation == SOCIALENUM.UNTRUSTED) {
                        output.success = COMMANDOUTPUTENUM.INFO;
                        output.message = "You have no relations with " + targetPlayer.getName();
                    } else {
                        social.setRelationTo(targetPlayerUUID, null); // Ensures that you don't get stray "Untrusted" values (saves memory)
                        output.message = "You no longer trust " + targetPlayer.getName();
                    }
                    break;
                case TRUSTENUM.GRANT:
                    if (playerUUID.equals(targetPlayerUUID)) {
                        executeFail(cmdSender, output, "Player has you blocked");
                        return true;
                    }

                    if (currentRelation.isTrustworthy()) { // Already Trusted
                        output.success = COMMANDOUTPUTENUM.INFO;
                        output.message = "You have already entrusted " + targetPlayer.getName();
                    } else if (theirRelation == SOCIALENUM.BLOCKED) { // They Blocked you
                        executeFail(cmdSender, output, "Player has you blocked.");
                        return true;
                    } else if (theirRelation == SOCIALENUM.TRUSTED) { // Make Players Allies
                        social.setRelationTo(targetPlayerUUID, SOCIALENUM.FRIENDS);
                        targetSocial.setRelationTo(playerUUID, SOCIALENUM.FRIENDS);
                        output.message = "You are now friends with " + targetPlayer.getName();

                        if (targetPlayer.getPlayer() instanceof Player targetOnline) {
                            RPCommandOutput targetMessage = new RPCommandOutput();
                            targetMessage.success = COMMANDOUTPUTENUM.TRUE;
                            targetMessage.message =  "You are now friends with " + player.getName();
                            targetOnline.sendRichMessage(targetMessage.toString());
                        }
                    } else { // Trust Player
                        social.setRelationTo(targetPlayerUUID, SOCIALENUM.TRUSTED);
                        output.message = "You have now entrusted " + targetPlayer.getName();
                    }
                    break;

                case TRUSTENUM.INFO:
                    throw new Exception();
            }

            social.saveChanges();
        } catch (Exception ignore) {
            output.success = COMMANDOUTPUTENUM.RAW;
            output.message = "\n<green>--- Trust List ---</green>\n";

            social.getRelationsToAll((k, v) -> k.equals(targetPlayerUUID)).forEach((k, v) -> {
                output.message += "- " + RPUtil.getUsernameFromCache(k) + ": " + v + "\n";
                // TODO: Make them glow locally when this command is ran
            });
        }

        cmdSender.sendRichMessage(output.toString());
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return switch (args.length) { // This is just preference
            case 0, 1 ->
                    new ArrayList<>(Arrays.stream(TRUSTENUM.values()).map(x -> x.name().toLowerCase(Locale.ROOT)).toList());
            case 2 ->
                    Bukkit.getOnlinePlayers().stream().filter(x -> !x.getUniqueId().equals(sender instanceof Player p ? p.getUniqueId() : null)).map(Player::getName).toList();
            default -> Collections.emptyList(); // Allowing unnecessary suggestions feels unfinished
        };
    }
}
