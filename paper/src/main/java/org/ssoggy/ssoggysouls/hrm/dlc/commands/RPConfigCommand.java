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

public class RPConfigCommand implements CommandExecutor, TabCompleter {
    // ⚡ Bolt: Cache enum string mappings to avoid redundant O(N) array allocations on every tab complete
    private static final List<String> OPTION_CONFIGS = Arrays.stream(OPTIONCONFIGENUM.values()).filter(x -> x.index > 0).map(x -> x.id).toList();
    private static final List<String> OPTION_EDITS = Arrays.stream(OPTIONEDITENUM.values()).map(x -> x.id).toList();

    private static final String OPT_STRUCTURE = "STRUCTURE";
    private static final String OPT_GAMERULE = "GAMERULE";
    private static final String OPT_TIMER = "TIMER";
    private static final String OPT_RELOAD = "RELOAD";
    private static final String CMD_PREFIX = "/revivalconfig ";
    private static final String ERR_MARKER_OPEN = " >>";
    private static final String ERR_MARKER_CLOSE = "<<";
    private static final String ERR_MARKER_EMPTY = " >><<";
    private static final String AQUA_LABEL_SUFFIX = ":</aqua> ";

    private static final List<String> LIST_BOOLEAN = List.of("true", "false");
    private static final List<String> LIST_BLOCKIDS = Arrays.stream(Material.values()).filter(Material::isBlock).map(Enum::name).toList();
    private static final Map<String, String> cmdKeywords = Map.ofEntries( // Keyword shortcuts used in the command
            Map.entry("structure", OPT_STRUCTURE),
            Map.entry("1", OPT_STRUCTURE),
            Map.entry("s", OPT_STRUCTURE),
            Map.entry("struc", OPT_STRUCTURE),
            Map.entry("struct", OPT_STRUCTURE),
            Map.entry("gamerule", OPT_GAMERULE),
            Map.entry("gamerules", OPT_GAMERULE),
            Map.entry("2", OPT_GAMERULE),
            Map.entry("g", OPT_GAMERULE),
            Map.entry("gr", OPT_GAMERULE),
            Map.entry("gmr", OPT_GAMERULE),
            Map.entry("reload", OPT_RELOAD),
            Map.entry("r", OPT_RELOAD),
            Map.entry("0", OPT_RELOAD),
            Map.entry("timer", OPT_TIMER),
            Map.entry("t", OPT_TIMER),
            Map.entry("3", OPT_TIMER)
    );


    private void executeReloadCMD(RPCommandOutput result) {
        RPConfig.init();
        result.success = COMMANDOUTPUTENUM.INFO;
        result.message = "reloading...";
        result.details = "Supposed to do some crazy reload logic but it's not implemented yet."; //Optional
    }

    private void executeTimerCMD(String who, String where, RPCommandOutput result) {
        if (!RPStatic.CONFIG_TIMERS.containsKey(where)) {
            result.success = COMMANDOUTPUTENUM.FALSE;
            result.message = "Invalid timer: " + where;
            return;
        }
        try {
            int whoInt = Integer.parseInt(who);
            byte status = RPConfig.setConfigTimer(where, whoInt);
            result.success = COMMANDOUTPUTENUM.valueOf(status);
            result.message = (status == 1) ? "Set " + where + " to " + whoInt : "Failed to update configuration.";
        } catch (NumberFormatException e) {
            result.success = COMMANDOUTPUTENUM.FALSE;
            result.message = "Timer value must be a number.";
        }
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
        OPTIONEDITENUM action = OPTIONEDITENUM.getEnumFromVal(what);
        if (!RPStatic.BLOCK_TAGS.containsKey(where)) {
            result.success = COMMANDOUTPUTENUM.FALSE;
            result.message = "Invalid structure: " + where;
            return;
        }
        if (action == null) {
            result.success = COMMANDOUTPUTENUM.FALSE;
            result.message = "Use add, remove, or reset.";
            return;
        }

        switch(action) {
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

    @SuppressWarnings("java:S3516") // onCommand always returns true by design (Bukkit CommandExecutor convention)
    @Override
    public boolean onCommand(@NotNull CommandSender cmdSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        RPCommandOutput result = new RPCommandOutput();
        String plOpt1 = args.length == 0 ? "" : args[0];
        String opt1 = cmdKeywords.getOrDefault(plOpt1.toLowerCase(), "");

        OPTIONCONFIGENUM option = OPTIONCONFIGENUM.getEnumFromVal(opt1);
        if (option == null) {
            result.success = COMMANDOUTPUTENUM.FALSE;
            result.message = "Please use /revivalconfig <structure|gamerule|timer|reload>";
        } else {
            switch (option) {
                case OPTIONCONFIGENUM.STRUCTURE -> handleStructure(args, result);
                case OPTIONCONFIGENUM.GAMERULE -> handleGamerule(args, result);
                case OPTIONCONFIGENUM.TIMER -> handleTimer(args, result);
                case OPTIONCONFIGENUM.RELOAD -> executeReloadCMD(result);
                default -> {
                    result.success = COMMANDOUTPUTENUM.FALSE;
                    result.message = "<red>Command Failed: " + CMD_PREFIX + ">>" + plOpt1 + "<</red>";
                    cmdSender.sendRichMessage(result.toString());
                    return true;
                }
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

    private void handleStructure(String[] args, RPCommandOutput result) {
        switch (args.length) {
            case 0, 1:
                result.success = COMMANDOUTPUTENUM.FALSE;
                result.message = CMD_PREFIX + (args.length > 0 ? args[0] : "") + ERR_MARKER_EMPTY;
                break;
            case 2:
                if (!RPStatic.BLOCK_TAGS.containsKey(args[1])) {
                    result.success = COMMANDOUTPUTENUM.FALSE;
                    result.message = CMD_PREFIX + args[0] + ERR_MARKER_OPEN + args[1] + ERR_MARKER_CLOSE;
                    break;
                }
                result.success = COMMANDOUTPUTENUM.INFO;
                result.message = "Contents inside of <aqua>" + args[1] + AQUA_LABEL_SUFFIX + RPStatic.BLOCK_TAGS.getOrDefault(args[1], Set.of()).stream().map(Enum::name).toList();
                break;
            case 3:
                if (!Objects.equals(args[2], "reset")) {
                    result.success = COMMANDOUTPUTENUM.FALSE;
                    result.message = CMD_PREFIX + args[0] + " " + args[1] + ERR_MARKER_OPEN + args[2] + ERR_MARKER_CLOSE;
                    break;
                }
                // reset action has exactly 3 args, so no material argument is available
                executeStructureCMD("", args[2], args[1], result);
                break;
            default:
                executeStructureCMD(args.length > 3 ? args[3].toUpperCase() : "", args[2], args[1], result); // All params are successfully entered
                break;
        }
    }

    private void handleGamerule(String[] args, RPCommandOutput result) {
        switch (args.length) {
            case 0, 1:
                result.success = COMMANDOUTPUTENUM.FALSE;
                result.message = CMD_PREFIX + (args.length > 0 ? args[0] : "") + ERR_MARKER_EMPTY;
                result.details = "Command is incomplete."; // Optional
                break;
            case 2:
                if (!RPStatic.CONFIG_RULES.containsKey(args[1])) {
                    result.success = COMMANDOUTPUTENUM.FALSE;
                    result.message = CMD_PREFIX + args[0] + ERR_MARKER_OPEN + args[1] + ERR_MARKER_CLOSE;
                    break;
                }
                result.success = COMMANDOUTPUTENUM.INFO;
                result.message = "<aqua>" + args[1] + AQUA_LABEL_SUFFIX + (RPStatic.CONFIG_RULES.get(args[1])).toString();
                break;
            default:
                executeGameruleCMD(args[2], args[1], result);
                break;
        }
    }

    private void handleTimer(String[] args, RPCommandOutput result) {
        switch (args.length) {
            case 0, 1:
                result.success = COMMANDOUTPUTENUM.FALSE;
                result.message = CMD_PREFIX + (args.length > 0 ? args[0] : "") + ERR_MARKER_EMPTY;
                result.details = "Command is incomplete."; // Optional
                break;
            case 2:
                if (!RPStatic.CONFIG_TIMERS.containsKey(args[1])) {
                    result.success = COMMANDOUTPUTENUM.FALSE;
                    result.message = CMD_PREFIX + args[0] + ERR_MARKER_OPEN + args[1] + ERR_MARKER_CLOSE;
                    break;
                }
                result.success = COMMANDOUTPUTENUM.INFO;
                result.message = "<aqua>" + args[1] + AQUA_LABEL_SUFFIX + (RPStatic.CONFIG_TIMERS.get(args[1])).toString() + "s";
                break;
            default:
                executeTimerCMD(args[2], args[1], result);
                break;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        String plOpt1 = args.length == 0 ? "" : args[0];
        String opt1 = cmdKeywords.getOrDefault(plOpt1.toLowerCase(), ""); // opt1 short for option 1

        return switch(args.length) {
            case 1 -> org.bukkit.util.StringUtil.copyPartialMatches(args[0], OPTION_CONFIGS, new java.util.ArrayList<>());
            case 2 -> {
                if (Objects.equals(opt1, OPT_STRUCTURE)) {
                    yield new java.util.ArrayList<>(RPStatic.BLOCK_TAGS.keySet());
                } else if (Objects.equals(opt1, OPT_GAMERULE)) {
                    yield new java.util.ArrayList<>(RPStatic.CONFIG_RULES.keySet());
                } else if (Objects.equals(opt1, OPT_TIMER)) {
                    yield new java.util.ArrayList<>(RPStatic.CONFIG_TIMERS.keySet());
                }
                yield Collections.emptyList();
            }
            case 3 -> {
                if (Objects.equals(opt1, OPT_STRUCTURE)) {
                    yield org.bukkit.util.StringUtil.copyPartialMatches(args[2], OPTION_EDITS, new java.util.ArrayList<>());
                } else if (Objects.equals(opt1, OPT_GAMERULE)) {
                    yield LIST_BOOLEAN;
                }
                yield Collections.emptyList();
            }
            case 4 -> {
                if (Objects.equals(opt1, OPT_STRUCTURE)) {
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
