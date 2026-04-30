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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ssoggy.ssoggysouls.hrm.dlc.enums.OPTIONCONFIGENUM;
import org.ssoggy.ssoggysouls.hrm.dlc.enums.OPTIONEDITENUM;
import org.ssoggy.ssoggysouls.hrm.dlc.enums.COMMANDOUTPUTENUM;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPConfig;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPCommandOutput;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStatic;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
// Note to self, rewrite again later

public class RPConfigCommand implements CommandExecutor, TabCompleter { // Lazy Command Write
    private static final List<String> LIST_BOOLEAN = List.of("true", "false");// TODO: 3 statics
    private static final List<String> LIST_BLOCKIDS = Arrays.stream(Material.values()).filter(Material::isBlock).map(Enum::name).toList();
    private static final Map<String, String> cmdKeywords = Map.ofEntries( // Keyword shortcuts used in the command
            Map.entry("structure", "STRUCTURE"),
            Map.entry("1", "STRUCTURE"),
            Map.entry("s", "STRUCTURE"),
            Map.entry("struc", "STRUCTURE"),
            Map.entry("struct", "STRUCTURE"),
            Map.entry("gamerule", "GAMERULE"),
            Map.entry("gamerules", "GAMERULE"),
            Map.entry("2", "GAMERULE"),
            Map.entry("g", "GAMERULE"),
            Map.entry("gr", "GAMERULE"),
            Map.entry("gmr", "GAMERULE"),
            Map.entry("reload", "RELOAD"),
            Map.entry("r", "RELOAD"),
            Map.entry("0", "RELOAD"),
            Map.entry("timer", "TIMER"),
            Map.entry("t", "TIMER"),
            Map.entry("3", "TIMER")
    );


    private void executeReloadCMD(RPCommandOutput result) {
        RPConfig.init();
        result.success = COMMANDOUTPUTENUM.INFO;
        result.message = "reloading...";
        result.details = "Supposed to do some crazy reload logic but it's not implemented yet."; //Optional
    }

    private void executeTimerCMD(String who, String where, RPCommandOutput result) {
        int whoInt = Integer.parseInt(who);
        result.success = COMMANDOUTPUTENUM.valueOf(RPConfig.setConfigTimer(where, whoInt));
        result.message = "Set " + where + " to " + whoInt;
    }

    private void executeGameruleCMD(String who, String where, RPCommandOutput result) {
        boolean whoBool = Boolean.parseBoolean(who);
        result.success = COMMANDOUTPUTENUM.valueOf(RPConfig.setConfigRule(where, whoBool));
        result.message = "Set " + where + " to " + whoBool;
    }

    private void executeStructureCMD(String who, String what, String where, RPCommandOutput result) {
        Set<Material> whoSet;
        Material whoMat;
        String whoName;
        switch(OPTIONEDITENUM.getEnumFromVal(what)) {
            case null:
                break;
            case OPTIONEDITENUM.ADD: // add
                if (!Objects.equals(who, "ALL")) {
                    whoMat = Material.getMaterial(who);
                    if (whoMat == null) {
                        result.success = COMMANDOUTPUTENUM.FALSE;
                        result.message = "Invalid BlockMaterial entered";
                        break;
                    }

                    whoSet = Stream.concat(RPStatic.BLOCK_TAGS.getOrDefault(where, Set.of()).stream(), Stream.of(whoMat)).collect(Collectors.toSet());
                    result.success = COMMANDOUTPUTENUM.valueOf(RPConfig.setBlockTag(where, whoSet));
                    whoName = whoMat.name();
                    result.message = "Added " + whoName + " to " + where;

                    break;
                }

                whoSet = Arrays.stream(Material.values()).filter(Material::isBlock).collect(Collectors.toSet());
                result.success = COMMANDOUTPUTENUM.valueOf(RPConfig.setBlockTag(where, whoSet));
                result.message = "Added everything to " + where;
                break;
            case OPTIONEDITENUM.REMOVE: // remove
                if (!Objects.equals(who, "ALL")) {
                    whoMat = Material.getMaterial(who);
                    if (whoMat == null) {
                        result.success = COMMANDOUTPUTENUM.FALSE;
                        result.message = "Invalid BlockMaterial entered";
                        return;
                    }

                    whoSet = RPStatic.BLOCK_TAGS.getOrDefault(where, Set.of());
                    whoSet.remove(whoMat);
                    result.success = COMMANDOUTPUTENUM.valueOf(RPConfig.setBlockTag(where, whoSet));
                    whoName = whoMat.name();
                    result.message = "Removed " + whoName + " from " + where;
                    return;
                }

                whoSet = Set.of();
                result.success = COMMANDOUTPUTENUM.valueOf(RPConfig.setBlockTag(where, whoSet));
                result.message = "Removed everything from " + where;
                break;
            case OPTIONEDITENUM.RESET: // reset
                result.success = COMMANDOUTPUTENUM.valueOf(RPConfig.resetBlockTag(where));
                result.message = "Reset values in " + where;
                break;
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender cmdSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        RPCommandOutput result = new RPCommandOutput();
        String plOpt1 = args.length == 0 ? "" : args[0];
        String opt1 = cmdKeywords.getOrDefault(plOpt1.toLowerCase(), "");

        switch(OPTIONCONFIGENUM.getEnumFromVal(opt1)) {
            case null -> {}
            case OPTIONCONFIGENUM.STRUCTURE -> { // STRUCTURE
                switch(args.length) {
                    case 0:
                    case 1:
                        result.success = COMMANDOUTPUTENUM.FALSE;
                        result.message = "/revivalconfig " + args[0] + " >><<";
                        break;
                    case 2:
                        if (!RPStatic.BLOCK_TAGS.containsKey(args[1])) {
                            result.success = COMMANDOUTPUTENUM.FALSE;
                            result.message = "/revivalconfig " + args[0] + " >>" + args[1] + "<<";
                            break;
                        }

                        result.success = COMMANDOUTPUTENUM.INFO;
                        result.message = "Contents inside of <aqua>" + args[1] + ":</aqua> " + RPStatic.BLOCK_TAGS.getOrDefault(args[1], Set.of()).stream().map(Enum::name).toList();
                        break;
                    case 3:
                        if (!Objects.equals(args[2], "reset")) {
                            result.success = COMMANDOUTPUTENUM.FALSE;
                            result.message = "/revivalconfig " + args[0] + " " + args[1] + " >>" + args[2] + "<<";
                            break;
                        }
                    default:
                        executeStructureCMD(args.length > 3 ? args[3].toUpperCase() : "", args[2], args[1], result); // All params are successfully entered
                        break;
                }
            }
            case OPTIONCONFIGENUM.GAMERULE -> { // GAMERULE
                switch(args.length) {
                    case 0:
                    case 1:
                        result.success = COMMANDOUTPUTENUM.FALSE;
                        result.message = "/revivalconfig " + args[0] + " >><<";
                        result.details = "Command is incomplete."; // Optional
                        break;
                    case 2:
                        if (!RPStatic.CONFIG_RULES.containsKey(args[1])) {
                            result.success = COMMANDOUTPUTENUM.FALSE;
                            result.message = "/revivalconfig " + args[0] + " >>" + args[1] +"<<";
                            break;
                        }

                        result.success = COMMANDOUTPUTENUM.INFO;
                        result.message = "<aqua>" + args[1] + ":</aqua> " + (RPStatic.CONFIG_RULES.get(args[1])).toString();
                        break;
                    default:
                        executeGameruleCMD(args[2], args[1], result);
                        break;
                }
            }
            case OPTIONCONFIGENUM.TIMER -> { // TIMERS
                switch(args.length) {
                    case 0:
                    case 1:
                        result.success = COMMANDOUTPUTENUM.FALSE;
                        result.message = "/revivalconfig " + args[0] + " >><<";
                        result.details = "Command is incomplete."; // Optional
                        break;
                    case 2:
                        if (!RPStatic.CONFIG_TIMERS.containsKey(args[1])) {
                            result.success = COMMANDOUTPUTENUM.FALSE;
                            result.message = "/revivalconfig " + args[0] + " >>" + args[1] +"<<";
                            break;
                        }

                        result.success = COMMANDOUTPUTENUM.INFO;
                        result.message = "<aqua>" + args[1] + ":</aqua> " + (RPStatic.CONFIG_TIMERS.get(args[1])).toString() + "s";
                        break;
                    default:
                        executeTimerCMD(args[2], args[1], result);
                        break;
                }
            }
            case OPTIONCONFIGENUM.RELOAD -> executeReloadCMD(result);
            default -> {
                result.success = COMMANDOUTPUTENUM.FALSE;
                result.message = "<red>Command Failed: /revivalconfig >>" + plOpt1 + "<<</red>";
                cmdSender.sendRichMessage(result.toString());
                return true;
            }
        }

        if (result.success == COMMANDOUTPUTENUM.NULL) {
            result.success = COMMANDOUTPUTENUM.FALSE;
            result.message = "Something went wrong, check the console for more details";
            RPStatic.LOGGER.severe(plOpt1);
        }

        cmdSender.sendRichMessage(result.toString()); // feedback
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        String plOpt1 = args.length == 0 ? "" : args[0];
        String opt1 = cmdKeywords.getOrDefault(plOpt1.toLowerCase(), ""); // opt1 short for option 1

        return switch(args.length) {
            case 1 -> Arrays.stream(OPTIONCONFIGENUM.values()).filter(x -> x.index > 0).map(x -> x.id).toList();
            case 2 -> {
                if (Objects.equals(opt1, "STRUCTURE")) {
                    yield new java.util.ArrayList<>(RPStatic.BLOCK_TAGS.keySet());
                } else if (Objects.equals(opt1, "GAMERULE")) {
                    yield new java.util.ArrayList<>(RPStatic.CONFIG_RULES.keySet());
                } else if (Objects.equals(opt1, "TIMER")) {
                    yield new java.util.ArrayList<>(RPStatic.CONFIG_TIMERS.keySet());
                }
                yield Collections.emptyList();
            }
            case 3 -> {
                if (Objects.equals(opt1, "STRUCTURE")) {
                    yield Arrays.stream(OPTIONEDITENUM.values()).map(x -> x.id).toList();
                } else if (Objects.equals(opt1, "GAMERULE")) {
                    yield LIST_BOOLEAN;
                }
                yield Collections.emptyList();
            }
            case 4 -> {
                if (Objects.equals(opt1, "STRUCTURE")) {
                    String opt = args[2];

                    if (Objects.equals(opt, "add")) {
                        yield LIST_BLOCKIDS;
                    } else if (Objects.equals(opt, "remove")) { // calculated at runtime
                        yield RPStatic.BLOCK_TAGS.getOrDefault(args[1], Set.of()).stream().map(Enum::name).toList();
                    }
                }
                yield Collections.emptyList();
            }
            default -> Collections.emptyList();
        };
    }
}
