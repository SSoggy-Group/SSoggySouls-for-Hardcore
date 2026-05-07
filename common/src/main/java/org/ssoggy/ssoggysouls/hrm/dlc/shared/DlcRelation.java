package org.ssoggy.ssoggysouls.hrm.dlc.shared;

public enum DlcRelation {
    BLOCKED,
    UNTRUSTED,
    FRIENDS,
    TRUSTED;

    public boolean isTrustworthy() {
        return this == TRUSTED || this == FRIENDS;
    }
}
