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

        // Also clean up DLC death state if possible
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                if (RPStatic.DEAD_LOCATIONS != null) {
                    RPStatic.DEAD_LOCATIONS.remove(data.getUuid());
                }
                if (RPStatic.DEAD_STORAGE != null) {
                    RPStatic.DEAD_STORAGE.removeValue(data.getUuid().toString(), "deathpos");
                    RPStatic.DEAD_STORAGE.removeValue(data.getUuid().toString(), "deathtime");
                    RPStatic.DEAD_STORAGE.saveConfig();
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to clear DLC death state for " + data.getUsername());
            }
        });

        if (target != null && target.isOnline()
                && (target.getGameMode() != GameMode.SURVIVAL || GAMEMODESENUM.getPlayerGameMode(target) == GAMEMODESENUM.GHOSTMODE)) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (target.isOnline()) {
                    plugin.getLimboDeadPlayers().remove(target.getUniqueId());
                    GAMEMODESENUM.setPlayerGameMode(target, GAMEMODESENUM.SURVIVAL);
                    target.sendMessage(MessageUtil.get("revive-success"));

                    if (plugin.isLimboServer()) {
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            if (target.isOnline()) {
                                ServerTransferUtil.sendToMain(target);
                            }
                        }, 40L);
                    }
                }
            });
        }
    }
}
