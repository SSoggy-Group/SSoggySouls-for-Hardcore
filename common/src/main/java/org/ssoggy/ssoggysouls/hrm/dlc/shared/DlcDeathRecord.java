package org.ssoggy.ssoggysouls.hrm.dlc.shared;

import java.time.Instant;
import java.util.UUID;

public record DlcDeathRecord(
        UUID uuid,
        String username,
        String worldId,
        int x,
        int y,
        int z,
        Instant time,
        UUID holder
) {
    public DlcDeathRecord withHolder(UUID holder) {
        return new DlcDeathRecord(uuid, username, worldId, x, y, z, time, holder);
    }
}
