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

@SuppressWarnings("java:S3776") // Cognitive complexity is irreducible due to deeply nested sub-command switch logic
public class RPConfigCommand implements CommandExecutor, TabCompleter {
    // ⚡ Bolt: Cache enum string mappings to avoid redundant O(N) array allocations on every tab complete
    private static final List<String> OPTION_CONFIGS = OPTIONCONFIGENUM.VALUES.stream().map(x -> x.id.toLowerCase(java.util.Locale.ROOT)).toList();
    private static final List<String> OPTION_EDITS = OPTIONEDITENUM.VALUES.stream().map(x -> x.id).toList();

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

    private void executeTimerCMD(String who, String where, RPCommandOutput result, String label) {
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
            result.message = "Timer value must be a number. Click to fix: <click:suggest_command:'/" + label + " timer " + where + " '><hover:show_text:'<gray>Click to auto-fill this command</gray>'><gray>/" + label + " timer " + where + " </gray></hover></click>";
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

                    whoSet = new java.util.HashSet<>(RPStatic.BLOCK_TAGS.getOrDefault(where, Set.of()));
                    whoSet.add(whoMat);
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
            result.message = "Please use <click:suggest_command:'/revivalconfig '><hover:show_text:'<gray>Click to auto-fill this command</gray>'><gray>/revivalconfig \\<structure|gamerule|timer|reload\\></gray></hover></click>";
        } else {
            switch (option) {
                case OPTIONCONFIGENUM.STRUCTURE -> handleStructure(args, result);
                case OPTIONCONFIGENUM.GAMERULE -> handleGamerule(args, result);
                case OPTIONCONFIGENUM.TIMER -> handleTimer(args, result, label);
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
                java.util.List<String> names = new java.util.ArrayList<>();
                for (Material mat : RPStatic.BLOCK_TAGS.getOrDefault(args[1], Set.of())) {
                    names.add(mat.name());
                }
                result.message = "Contents inside of <aqua>" + args[1] + AQUA_LABEL_SUFFIX + names;
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

    private void handleTimer(String[] args, RPCommandOutput result, String label) {
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
                executeTimerCMD(args[2], args[1], result, label);
                break;
        }
    }

    @Override
    @SuppressWarnings("java:S138") // Large switch block makes refactoring pointless
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        String plOpt1 = args.length == 0 ? "" : args[0];
        String opt1 = cmdKeywords.getOrDefault(plOpt1.toLowerCase(), ""); // opt1 short for option 1

        return switch(args.length) {
            case 1 -> org.ssoggy.ssoggysouls.util.TabCompleteUtil.filterStartsWith(OPTION_CONFIGS, args[0]);
            case 2 -> {
                if (Objects.equals(opt1, OPT_STRUCTURE)) {
                    yield org.ssoggy.ssoggysouls.util.TabCompleteUtil.filterStartsWith(RPStatic.BLOCK_TAGS.keySet(), args[1]);
                } else if (Objects.equals(opt1, OPT_GAMERULE)) {
                    yield org.ssoggy.ssoggysouls.util.TabCompleteUtil.filterStartsWith(RPStatic.CONFIG_RULES.keySet(), args[1]);
                } else if (Objects.equals(opt1, OPT_TIMER)) {
                    yield org.ssoggy.ssoggysouls.util.TabCompleteUtil.filterStartsWith(RPStatic.CONFIG_TIMERS.keySet(), args[1]);
                }
                yield Collections.emptyList();
            }
            case 3 -> {
                if (Objects.equals(opt1, OPT_STRUCTURE)) {
                    yield org.ssoggy.ssoggysouls.util.TabCompleteUtil.filterStartsWith(OPTION_EDITS, args[2]);
                } else if (Objects.equals(opt1, OPT_GAMERULE)) {
                    yield org.ssoggy.ssoggysouls.util.TabCompleteUtil.filterStartsWith(LIST_BOOLEAN, args[2]);
                }
                yield Collections.emptyList();
            }
            case 4 -> {
                if (Objects.equals(opt1, OPT_STRUCTURE)) {
                    String opt = args[2];

                    if (Objects.equals(opt, "add")) {
                        yield org.ssoggy.ssoggysouls.util.TabCompleteUtil.filterStartsWith(LIST_BLOCKIDS, args[3]);
                    } else if (Objects.equals(opt, "remove")) { // calculated at runtime
                        String prefix = args[3];
                        java.util.List<String> removals = new java.util.ArrayList<>();
                        for (Material mat : RPStatic.BLOCK_TAGS.getOrDefault(args[1], Set.of())) {
                            String name = mat.name();
                            if (name.regionMatches(true, 0, prefix, 0, prefix.length())) {
                                removals.add(name);
                            }
                        }
                        yield removals;
                    }
                }
                yield Collections.emptyList();
            }
            default -> Collections.emptyList();
        };
    }
}
