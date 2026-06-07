/*
RevivePlus by Cera and Jakeccz
Copyright (C) 2026 Commune

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with RevivePlus.  If not, see <https://www.gnu.org/licenses/>
 */

package org.ssoggy.ssoggysouls.hrm.dlc.util;

import com.google.common.collect.Maps;
import org.ssoggy.ssoggysouls.hrm.dlc.enums.SOCIALENUM;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiPredicate;

public class RPSocial {
    private final UUID storedUuid;
    public RPSocial(UUID uuid) {
        this.storedUuid = uuid;
    }

    public void setRelationTo(UUID uuid, @Nullable SOCIALENUM relationship) {
        if (relationship == null) { // technically could be another function but I enjoy flexible code
            RPStatic.SOCIAL_STORAGE.removeValue(this.storedUuid.toString(), uuid.toString());
            return;
        }
        if (RPStatic.SOCIAL_STORAGE.setValueIfChanged(this.storedUuid.toString(), uuid.toString(), relationship.name())) {
            RPStatic.SOCIAL_STORAGE.saveConfig();
        }
    }

    private SOCIALENUM riskyOrDefault(String risky, SOCIALENUM def) {
        try {
            return SOCIALENUM.valueOf(risky);
        } catch (Exception ignored) {
            return def;
        }
    }

    public SOCIALENUM getRelationTo(UUID uuid) {
        return riskyOrDefault(RPStatic.SOCIAL_STORAGE.getValue(this.storedUuid.toString(), uuid.toString()), SOCIALENUM.UNTRUSTED);
    }

    public Map<UUID, SOCIALENUM> getRelationsToAll(@Nullable BiPredicate<? super UUID, ? super SOCIALENUM> filter) {
        try {
            Map<UUID, SOCIALENUM> result = Maps.newHashMap();
            RPStatic.SOCIAL_STORAGE.getTable(this.storedUuid.toString()).forEach((rawKey, rawValue) -> {
                UUID k = UUID.fromString(rawKey);
                SOCIALENUM v = riskyOrDefault((String) rawValue, SOCIALENUM.UNTRUSTED);
                if (filter == null || filter.test(k, v)) {
                    result.put(k, v);
                }
            });
            return result;
        } catch (Exception ignore) {
            return Maps.newHashMapWithExpectedSize(0);
        }
    }

    public void saveChanges() {
        RPStatic.SOCIAL_STORAGE.saveConfig();
    }
}
