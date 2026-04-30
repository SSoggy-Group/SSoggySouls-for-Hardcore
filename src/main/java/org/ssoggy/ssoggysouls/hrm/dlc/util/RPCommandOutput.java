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

import org.ssoggy.ssoggysouls.hrm.dlc.enums.COMMANDOUTPUTENUM;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings("java:S1104") // Intentional: mutable DTO fields set directly by command handlers
public class RPCommandOutput {
    public COMMANDOUTPUTENUM success = COMMANDOUTPUTENUM.NULL;
    public String message = "";
    public String messageColour = "";
    public String details = "";

    @Override
    public String toString() {
        String result = switch (this.success) {
            case FALSE -> {
                if (Objects.equals(this.messageColour, "")) { this.messageColour = "red"; }
                yield "<" + this.messageColour + ">" + "Failure. " + this.message + "</" + this.messageColour + ">";
            }
            case TRUE -> {
                if (Objects.equals(this.messageColour, "")) { this.messageColour = "green"; }
                yield "<" + this.messageColour + ">" + "Success! " + this.message + "</" + this.messageColour + ">";
            }
            case INFO, RAW -> {
                if (Objects.equals(this.messageColour, "")) { this.messageColour = "gray"; }
                yield "<" + this.messageColour + ">" + this.message + "</" + this.messageColour + ">";
            }
            default -> "";
        };

        if (this.success == COMMANDOUTPUTENUM.RAW) { return result; }
        return "\n" + Arrays.stream(result.split("\n")).map(x -> RPStatic.PREFIX + " " + x + "\n").collect(Collectors.joining());
    }
}