package org.ssoggy.ssoggysouls.hrm.dlc.shared;

public enum DlcStat {
    KILLS("kills"),
    DEATHS("deaths"),
    REVIVES("revives"),
    RITUAL_STARTED("ritual_started"),
    RITUAL_COMPLETED("ritual_completed"),
    LEVEL("level"),
    FRIEND_COUNT("friend_count"),
    BOUNTY_CLAIMED("bounty_claimed"),
    BOUNTY_PLACED("bounty_placed"),
    TOTAL_BOUNTY("total_bounty");

    private final String identifier;

    DlcStat(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public int getValue() {
        return ordinal();
    }

    /**
     * Example logic to return placeholder data for HRM placeholders
     */
    public String getAsPlaceholder() {
        // Example: DlcStat with value 10 -> "10"
        return String.valueOf(getValue());
    }
}
