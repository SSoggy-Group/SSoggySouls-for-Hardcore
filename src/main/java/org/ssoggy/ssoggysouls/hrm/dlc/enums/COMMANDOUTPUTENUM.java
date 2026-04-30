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

public enum COMMANDOUTPUTENUM {
    NULL((byte) -1),
    FALSE((byte) 0), // -1 is false
    TRUE((byte) 1), // 0 is true
    INFO((byte) 2),
    RAW((byte) 3);

    private final byte index;

    COMMANDOUTPUTENUM(byte i) {
        this.index = i;
    }

    public static COMMANDOUTPUTENUM valueOf(byte i) {
        return switch(i) {
            case 0 -> COMMANDOUTPUTENUM.FALSE;
            case 1 -> COMMANDOUTPUTENUM.TRUE;
            case 2 -> COMMANDOUTPUTENUM.INFO;
            case 3 -> COMMANDOUTPUTENUM.RAW;
            default -> COMMANDOUTPUTENUM.NULL;
        };
    }
}