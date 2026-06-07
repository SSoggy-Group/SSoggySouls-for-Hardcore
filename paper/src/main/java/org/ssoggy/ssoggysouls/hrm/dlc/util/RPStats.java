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

import org.ssoggy.ssoggysouls.hrm.dlc.enums.STATSENUM;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class RPStats {
    private final RPStorage storage;
    private final String table;
    public RPStats(UUID uuid) {
        this.storage = RPStatic.STATS_STORAGE;
        this.table = uuid.toString().replace("-", "");
    }

    public Map<STATSENUM, String> getAllStats() {
        Map<STATSENUM, String> map = new EnumMap<>(STATSENUM.class);
        for (STATSENUM num : STATSENUM.values())
            map.put(num, this.storage.getValue(this.table, num.name()));
        return map;
    }

    public String getStat(STATSENUM option) {
        return storage.getValue(this.table, option.name());
    }

    public void overrideStat(STATSENUM option, Object value) {
        if (storage.setValueIfChanged(this.table, option.name(), value)) {
            storage.saveConfig();
        }
    }

    public double incrementStat(STATSENUM option, double increment) {
        try {
            String oldVal = this.getStat(option);

            double newVal = (oldVal == null || oldVal.isBlank()) ? increment : Double.parseDouble(oldVal) + increment;
            this.overrideStat(option, newVal);
            return newVal;
        } catch (NumberFormatException e) {
            return Double.NEGATIVE_INFINITY;
        }
    }

    public double decrementStat(STATSENUM option, double increment) {
        return incrementStat(option, -increment);
    }
}
