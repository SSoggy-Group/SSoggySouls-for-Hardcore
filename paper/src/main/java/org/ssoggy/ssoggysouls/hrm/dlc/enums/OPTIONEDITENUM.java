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

import java.util.Objects;

public enum OPTIONEDITENUM {
    ADD("add", (byte)0),
    REMOVE("remove", (byte)1),
    RESET("reset", (byte)2);

    public static final java.util.List<OPTIONEDITENUM> VALUES = java.util.List.of(values());

    public final String id;
    public final byte index;

    OPTIONEDITENUM(String v, byte i) {
        this.id = v;
        this.index = i;
    }

    public static byte getIndex(String opt) {
        for (OPTIONEDITENUM n : VALUES) {
            if (Objects.equals(n.id, opt)) {
                return n.index;
            }
        }
        return (byte)-1;
    }

    public static OPTIONEDITENUM getEnumFromVal(String opt) {
        for (OPTIONEDITENUM n : VALUES) {
            if (Objects.equals(n.id, opt)) {
                return n;
            }
        }
        return null;
    }

    public static String getValue(byte i) {
        for (OPTIONEDITENUM n : VALUES) {
            if (n.index == i) {
                return n.id;
            }
        }
        return "";
    }
}