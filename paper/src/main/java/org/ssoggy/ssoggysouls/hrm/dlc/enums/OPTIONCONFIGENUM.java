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

package org.ssoggy.ssoggysouls.hrm.dlc.enums;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum OPTIONCONFIGENUM {
    STRUCTURE("STRUCTURE", (byte)1),
    GAMERULE("GAMERULE", (byte)2),
    TIMER("TIMER", (byte)3),
    RELOAD("RELOAD", (byte)0);

    public static final java.util.List<OPTIONCONFIGENUM> VALUES = java.util.Collections.unmodifiableList(java.util.Arrays.asList(values()));
    private static final Map<String, OPTIONCONFIGENUM> BY_ID;

    static {
        Map<String, OPTIONCONFIGENUM> map = new HashMap<>();
        for (OPTIONCONFIGENUM n : VALUES) {
            map.put(n.id, n);
        }
        BY_ID = Collections.unmodifiableMap(map);
    }

    public final String id;
    public final byte index;

    OPTIONCONFIGENUM(String v, byte i) {
        this.id = v;
        this.index = i;
    }

    public static byte getIndex(String opt) {
        if (opt == null) {
            return (byte)-1;
        }
        OPTIONCONFIGENUM val = BY_ID.get(opt);
        return val != null ? val.index : (byte)-1;
    }

    public static OPTIONCONFIGENUM getEnumFromVal(String opt) {
        if (opt == null) {
            return null;
        }
        return BY_ID.get(opt);
    }

    public static String getValue(byte i) {
        for (OPTIONCONFIGENUM n : VALUES) {
            if (n.index == i) {
                return n.id;
            }
        }
        return "";
    }
}
