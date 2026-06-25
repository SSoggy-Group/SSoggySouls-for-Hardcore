package org.ssoggy.ssoggysouls.hrm.dlc.shared;

public enum DlcStat {
    KILLS,
    DEATHS,
    REVIVES,
    RITUAL_STARTED,
    RITUAL_COMPLETED,
    LEVEL,
    FRIEND_COUNT,
    BOUNTY_CLAIMED,
    BOUNTY_PLACED,
    TOTAL_BOUNTY;

    public static final java.util.List<DlcStat> VALUES = java.util.Collections.unmodifiableList(java.util.Arrays.asList(values()));
}
