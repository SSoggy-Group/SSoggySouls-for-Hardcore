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
        storage.setValue(table, KEY_USERNAME, username);
        storage.setValue(table, KEY_WORLD, worldId);
        storage.setValue(table, KEY_X, x);
        storage.setValue(table, KEY_Y, y);
        storage.setValue(table, KEY_Z, z);
        storage.setValue(table, KEY_TIME, deathRecord.time().toString());
        storage.removeValue(table, KEY_HOLDER);
        storage.save();
    }

    public static void clearDeath(UUID uuid) {
        DEATHS.remove(uuid);
        DlcStorage storage = DlcServices.deathStorage();
        String table = uuid.toString();
        storage.removeValue(table, KEY_USERNAME);
        storage.removeValue(table, KEY_WORLD);
        storage.removeValue(table, KEY_X);
        storage.removeValue(table, KEY_Y);
        storage.removeValue(table, KEY_Z);
        storage.removeValue(table, KEY_TIME);
        storage.removeValue(table, KEY_HOLDER);
        storage.save();
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
        List<DlcDeathRecord> result = new ArrayList<>();
        for (DlcDeathRecord deathRecord : DEATHS.values()) {
            if (deathRecord.uuid().equals(viewerUuid)) {
                result.add(deathRecord);
                continue;
            }

            DlcRelation relationship = new DlcSocial(deathRecord.uuid()).getRelationTo(viewerUuid);
            Instant deathTime = deathRecord.time();
            boolean visible = deathTime.isBefore(now.minusSeconds(publicAfterSeconds))
                    || (relationship == DlcRelation.FRIENDS && deathTime.isBefore(now.minusSeconds(friendsAfterSeconds)))
                    || (relationship == DlcRelation.TRUSTED && deathTime.isBefore(now.minusSeconds(trustedAfterSeconds)));
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
