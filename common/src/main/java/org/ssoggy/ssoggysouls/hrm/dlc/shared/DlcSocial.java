package org.ssoggy.ssoggysouls.hrm.dlc.shared;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiPredicate;

public class DlcSocial {
    private final UUID owner;

    public DlcSocial(UUID owner) {
        this.owner = owner;
    }

    public void setRelationTo(UUID uuid, DlcRelation relation) {
        if (relation == null) {
            DlcServices.socialStorage().removeValue(owner.toString(), uuid.toString());
        } else {
            DlcServices.socialStorage().setValue(owner.toString(), uuid.toString(), relation.name());
        }
    }

    public DlcRelation getRelationTo(UUID uuid) {
        String value = DlcServices.socialStorage().getValue(owner.toString(), uuid.toString());
        try {
            return value == null ? DlcRelation.UNTRUSTED : DlcRelation.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return DlcRelation.UNTRUSTED;
        }
    }

    public Map<UUID, DlcRelation> getRelationsToAll(BiPredicate<? super UUID, ? super DlcRelation> filter) {
        Map<UUID, DlcRelation> result = new HashMap<>();
        DlcServices.socialStorage().getTable(owner.toString()).forEach((rawKey, rawValue) -> {
            try {
                UUID uuid = UUID.fromString(rawKey);
                DlcRelation relation = rawValue == null ? DlcRelation.UNTRUSTED : DlcRelation.valueOf(rawValue);
                if (filter == null || filter.test(uuid, relation)) {
                    result.put(uuid, relation);
                }
            } catch (IllegalArgumentException ignored) {
                // Skip corrupt entries.
            }
        });
        return result;
    }

    public void saveChanges() {
        DlcServices.socialStorage().save();
    }
}
