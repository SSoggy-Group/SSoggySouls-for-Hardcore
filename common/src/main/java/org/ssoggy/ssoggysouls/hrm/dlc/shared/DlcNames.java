package org.ssoggy.ssoggysouls.hrm.dlc.shared;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class DlcNames {
    private static final String TABLE = "usernamecache";

    private DlcNames() {
    }

    public static void cache(UUID uuid, String username) {
        if (uuid == null || username == null || username.isBlank()) {
            return;
        }

        DlcServices.usernameStorage().setValue(TABLE, uuid.toString(), username);
        DlcServices.usernameStorage().save();
    }

    public static String get(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return DlcServices.usernameStorage().getValue(TABLE, uuid.toString());
    }

    public static String getOrDefault(UUID uuid, String fallback) {
        String cached = get(uuid);
        return cached == null || cached.isBlank() ? fallback : cached;
    }

    public static Optional<UUID> findUuidByName(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }

        String normalized = username.trim();
        for (Map.Entry<String, String> entry : DlcServices.usernameStorage().getTable(TABLE).entrySet()) {
            if (entry.getValue() != null && entry.getValue().equalsIgnoreCase(normalized)) {
                try {
                    return Optional.of(UUID.fromString(entry.getKey()));
                } catch (IllegalArgumentException ignored) {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }
}
