package org.ssoggy.ssoggysouls.hrm.dlc.shared;

import java.util.Locale;
import java.util.Optional;

public enum DlcTrustAction {
    GRANT,
    REVOKE,
    INFO,
    BLOCK;

    public static final java.util.List<DlcTrustAction> VALUES = java.util.Collections.unmodifiableList(java.util.Arrays.asList(values()));

    public static Optional<DlcTrustAction> fromInput(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(valueOf(input.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}
