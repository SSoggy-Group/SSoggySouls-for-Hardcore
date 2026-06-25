package org.ssoggy.ssoggysouls.hrm.dlc.shared;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class DlcStats {
    private final String table;

    public DlcStats(UUID uuid) {
        this.table = uuid.toString().replace("-", "");
    }

    public Map<DlcStat, String> getAllStats() {
        Map<DlcStat, String> map = new EnumMap<>(DlcStat.class);
        for (DlcStat stat : DlcStat.VALUES) {
            map.put(stat, getStat(stat));
        }
        return map;
    }

    public String getStat(DlcStat stat) {
        return DlcServices.statsStorage().getValue(table, stat.name());
    }

    public void overrideStat(DlcStat stat, Object value) {
        if (DlcServices.statsStorage().setValueIfChanged(table, stat.name(), value)) {
            DlcServices.statsStorage().save();
        }
    }

    public double incrementStat(DlcStat stat, double increment) {
        try {
            String oldValue = getStat(stat);
            double newValue = oldValue == null || oldValue.isBlank()
                    ? increment
                    : Double.parseDouble(oldValue) + increment;
            overrideStat(stat, newValue);
            return newValue;
        } catch (NumberFormatException e) {
            return Double.NEGATIVE_INFINITY;
        }
    }

    public double decrementStat(DlcStat stat, double increment) {
        return incrementStat(stat, -increment);
    }
}
