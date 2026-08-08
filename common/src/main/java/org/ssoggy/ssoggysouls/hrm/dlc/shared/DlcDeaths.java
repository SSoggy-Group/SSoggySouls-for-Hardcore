package org.ssoggy.ssoggysouls.hrm.dlc.shared;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class DlcDeaths {
    private static final String KEY_USERNAME = "username";
    private static final String KEY_WORLD = "world";
    private static final String KEY_X = "x";
    private static final String KEY_Y = "y";
    private static final String KEY_Z = "z";
    private static final String KEY_TIME = "time";
    private static final String KEY_HOLDER = "holder";
    private static final Map<UUID, DlcDeathRecord> DEATHS = new ConcurrentHashMap<>();

    private DlcDeaths() {
    }

    public static void reloadFromStorage() {
        DEATHS.clear();
        for (String table : DlcServices.deathStorage().getTables()) {
            try {
                UUID uuid = UUID.fromString(table);
                Map<String, String> values = DlcServices.deathStorage().getTable(table);
                String username = values.get(KEY_USERNAME);
                String world = values.get(KEY_WORLD);
                String time = values.get(KEY_TIME);
                if (world == null || time == null) {
                    continue;
                }

                UUID holder = null;
                if (values.get(KEY_HOLDER) != null) {
                    holder = UUID.fromString(values.get(KEY_HOLDER));
                }

                DlcDeathRecord deathRecord = new DlcDeathRecord(
                        uuid,
                        username,
                        world,
                        Integer.parseInt(values.get(KEY_X)),
                        Integer.parseInt(values.get(KEY_Y)),
                        Integer.parseInt(values.get(KEY_Z)),
                        Instant.parse(time),
                        holder
                );
                DEATHS.put(uuid, deathRecord);
            } catch (RuntimeException e) {
                DlcServices.logger().warning("Skipping invalid RevivalPlus death record: " + table);
            }
        }
    }

    public static void recordDeath(UUID uuid, String username, String worldId, int x, int y, int z) {
        DlcDeathRecord deathRecord = new DlcDeathRecord(uuid, username, worldId, x, y, z, Instant.now(), null);
        DEATHS.put(uuid, deathRecord);
        DlcNames.cache(uuid, username);

        DlcStorage storage = DlcServices.deathStorage();
        String table = uuid.toString();
        boolean changed = storage.setValueIfChanged(table, KEY_USERNAME, username);
        changed |= storage.setValueIfChanged(table, KEY_WORLD, worldId);
        changed |= storage.setValueIfChanged(table, KEY_X, x);
        changed |= storage.setValueIfChanged(table, KEY_Y, y);
        changed |= storage.setValueIfChanged(table, KEY_Z, z);
        changed |= storage.setValueIfChanged(table, KEY_TIME, deathRecord.time().toString());
        changed |= storage.setValueIfChanged(table, KEY_HOLDER, null);
        if (changed) {
            storage.save();
        }
    }

    public static void clearDeath(UUID uuid) {
        DEATHS.remove(uuid);
        DlcStorage storage = DlcServices.deathStorage();
        String table = uuid.toString();
        boolean changed = storage.setValueIfChanged(table, KEY_USERNAME, null);
        changed |= storage.setValueIfChanged(table, KEY_WORLD, null);
        changed |= storage.setValueIfChanged(table, KEY_X, null);
        changed |= storage.setValueIfChanged(table, KEY_Y, null);
        changed |= storage.setValueIfChanged(table, KEY_Z, null);
        changed |= storage.setValueIfChanged(table, KEY_TIME, null);
        changed |= storage.setValueIfChanged(table, KEY_HOLDER, null);
        if (changed) {
            storage.save();
        }
    }

    public static void setHolder(UUID deadUuid, UUID holderUuid) {
        DlcDeathRecord deathRecord = DEATHS.get(deadUuid);
        if (deathRecord == null) {
            return;
        }
        if (Objects.equals(deathRecord.holder(), holderUuid)) {
            return;
        }
        DEATHS.put(deadUuid, deathRecord.withHolder(holderUuid));
        if (holderUuid == null) {
            if (DlcServices.deathStorage().hasValue(deadUuid.toString(), KEY_HOLDER)) {
                DlcServices.deathStorage().removeValue(deadUuid.toString(), KEY_HOLDER);
                DlcServices.deathStorage().save();
            }
        } else {
            if (DlcServices.deathStorage().setValueIfChanged(deadUuid.toString(), KEY_HOLDER, holderUuid.toString())) {
                DlcServices.deathStorage().save();
            }
        }
    }

    public static List<DlcDeathRecord> visibleDeaths(UUID viewerUuid,
                                                     long trustedAfterSeconds,
                                                     long friendsAfterSeconds,
                                                     long publicAfterSeconds) {
        Instant now = Instant.now();
        // ⚡ Bolt: Hoist threshold calculations outside the loop to avoid redundant object allocations
        Instant publicThreshold = now.minusSeconds(publicAfterSeconds);
        Instant friendsThreshold = now.minusSeconds(friendsAfterSeconds);
        Instant trustedThreshold = now.minusSeconds(trustedAfterSeconds);

        List<DlcDeathRecord> result = new ArrayList<>();
        for (DlcDeathRecord deathRecord : DEATHS.values()) {
            if (deathRecord.uuid().equals(viewerUuid)) {
                result.add(deathRecord);
                continue;
            }

            Instant deathTime = deathRecord.time();

            // ⚡ Bolt: Short-circuit on cheap time check to completely bypass the expensive DlcSocial lookup for older deaths
            if (deathTime.isBefore(publicThreshold)) {
                result.add(deathRecord);
                continue;
            }

            DlcRelation relationship = new DlcSocial(deathRecord.uuid()).getRelationTo(viewerUuid);
            boolean visible = (relationship == DlcRelation.FRIENDS && deathTime.isBefore(friendsThreshold))
                    || (relationship == DlcRelation.TRUSTED && deathTime.isBefore(trustedThreshold));

            if (visible) {
                result.add(deathRecord);
            }
        }
        result.sort(Comparator.comparing(DlcDeathRecord::time));
        return result;
    }

    public static List<DlcDeathRecord> allDeaths() {
        List<DlcDeathRecord> result = new ArrayList<>(DEATHS.values());
        result.sort(Comparator.comparing(DlcDeathRecord::time));
        return result;
    }
}
