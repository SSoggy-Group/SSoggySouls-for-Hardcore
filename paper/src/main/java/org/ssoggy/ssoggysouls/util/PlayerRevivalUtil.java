package org.ssoggy.ssoggysouls.util;

import org.ssoggy.ssoggysouls.SSoggySouls;
import org.ssoggy.ssoggysouls.model.PlayerData;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStatic;
import org.ssoggy.ssoggysouls.hrm.dlc.enums.GAMEMODESENUM;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public final class PlayerRevivalUtil {

    private PlayerRevivalUtil() {
    }

    /**
     * restores an online spectator to survival and optionally transfers them from
     * limbo.
     *
     * @param plugin the SSoggySouls plugin instance
     * @param data   the player data
     */
    public static void restoreOnlineSpectator(SSoggySouls plugin, PlayerData data) {
        Player target = Bukkit.getPlayer(data.getUuid());

        clearDlcDeathState(plugin, data.getUuid(), data.getUsername());

        if (target != null && target.isOnline() && shouldRestore(target)) {
            executeRevival(plugin, target);
        }
    }

    private static void clearDlcDeathState(SSoggySouls plugin, java.util.UUID uuid, String username) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                if (RPStatic.DEAD_LOCATIONS != null) {
                    RPStatic.DEAD_LOCATIONS.remove(uuid);
                }
                if (RPStatic.DEAD_STORAGE != null) {
                    RPStatic.DEAD_STORAGE.removeValue(uuid.toString(), "deathpos");
                    RPStatic.DEAD_STORAGE.removeValue(uuid.toString(), "deathtime");
                    RPStatic.DEAD_STORAGE.saveConfig();
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to clear DLC death state for " + username);
            }
        });
    }

    private static boolean shouldRestore(Player target) {
        return target.getGameMode() != GameMode.SURVIVAL || GAMEMODESENUM.getPlayerGameMode(target) == GAMEMODESENUM.GHOSTMODE;
    }

    private static void executeRevival(SSoggySouls plugin, Player target) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (target.isOnline()) {
                GAMEMODESENUM.setPlayerGameMode(target, GAMEMODESENUM.SURVIVAL);
                target.sendMessage(MessageUtil.get("revive-success"));

                if (plugin.isLimboServer()) {
                    scheduleTransfer(plugin, target);
                }
            }
        });
    }

    private static void scheduleTransfer(SSoggySouls plugin, Player target) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (target.isOnline()) {
                ServerTransferUtil.sendToMain(target);
            }
        }, 40L);
    }
}
