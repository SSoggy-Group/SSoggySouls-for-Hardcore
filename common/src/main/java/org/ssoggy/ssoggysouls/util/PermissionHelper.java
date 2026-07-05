package org.ssoggy.ssoggysouls.util;

import java.util.List;

/**
 * Common logic for permission and security checks.
 */
public abstract class PermissionHelper {

    /**
     * Checks if a player should be allowed based on trusted admin lists.
     *
     * @param uuid The player's UUID string
     * @param name The player's name (should be lowercase)
     * @param trustedAdmins The list of trusted admins from config
     * @return true if the player is trusted
     */
    protected static boolean isTrustedAdmin(String uuid, String name, List<String> trustedAdmins) {
        if (trustedAdmins == null || trustedAdmins.isEmpty()) {
            return false;
        }
        return trustedAdmins.contains(uuid) || trustedAdmins.contains(name);
    }
}
