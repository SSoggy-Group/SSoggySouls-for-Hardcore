package org.ssoggy.ssoggysouls.hrm.dlc.shared;

/**
 * Shared logic for ghost mode restrictions.
 */
public final class GhostRestrictionLogic {
    private GhostRestrictionLogic() {
    }

    /** The message shown to players when they are teleported back to their death location. */
    public static final String RESTRICTION_MESSAGE = "You may not travel that far away from your death location";

    /**
     * Checks if a ghost player has traveled beyond the allowed radius from their death location.
     *
     * @param deathX      X coordinate of death location
     * @param deathY      Y coordinate of death location
     * @param deathZ      Z coordinate of death location
     * @param currentX    Current X coordinate of player
     * @param currentY    Current Y coordinate of player
     * @param currentZ    Current Z coordinate of player
     * @param maxDistance Maximum allowed distance from death location
     * @return true if the player is beyond the max distance
     */
    public static boolean isOutOfBounds(double deathX, double deathY, double deathZ,
                                        double currentX, double currentY, double currentZ,
                                        double maxDistance) {
        double dx = deathX - currentX;
        double dy = deathY - currentY;
        double dz = deathZ - currentZ;
        return (dx * dx + dy * dy + dz * dz) > (maxDistance * maxDistance);
    }
}
