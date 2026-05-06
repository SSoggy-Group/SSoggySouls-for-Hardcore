package org.ssoggy.ssoggysouls.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimeUtil {

    private static final Pattern TIME_PATTERN = Pattern.compile("^(?:\\d+[hms])++$");
    private static final Pattern COMPONENT_PATTERN = Pattern.compile("(\\d+)([hms])");

    private TimeUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static long parseTimeToMillis(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return -1;
        }

        timeStr = timeStr.trim().toLowerCase();

        if (timeStr.startsWith("-")) return -1; // reject negative inputs before regex can strip the sign

        try {
            long hours = Long.parseLong(timeStr);
            if (hours < 0) return -1;
            return Math.multiplyExact(hours, 3600_000L);
        } catch (NumberFormatException e) {
            // not a plain integer
        } catch (ArithmeticException e) {
            return -1; // overflow
        }

        if (!TIME_PATTERN.matcher(timeStr).matches()) {
            return -1;
        }

        // parse time components
        Matcher matcher = COMPONENT_PATTERN.matcher(timeStr);
        long totalMillis = 0;
        boolean foundAny = false;

        while (matcher.find()) {
            foundAny = true;
            try {
                long value = Long.parseLong(matcher.group(1));
                if (value < 0) return -1;
                String unit = matcher.group(2);

                long addedMillis = switch (unit) {
                    case "h" -> Math.multiplyExact(value, 3600_000L);
                    case "m" -> Math.multiplyExact(value, 60_000L);
                    case "s" -> Math.multiplyExact(value, 1000L);
                    default -> 0L;
                };
                totalMillis = Math.addExact(totalMillis, addedMillis);
            } catch (NumberFormatException e) {
                // ignore
            } catch (ArithmeticException e) {
                return -1; // overflow
            }
        }

        return foundAny ? totalMillis : -1;
    }


    public static String formatTime(long millis) {
        if (millis < 0) return "0s";  //
        if (millis == 0) return "0s";

        long hours = millis / 3600_000L;
        long minutes = (millis % 3600_000L) / 60_000L;
        long seconds = (millis % 60_000L) / 1000L;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append("h");
        }
        if (minutes > 0) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(minutes).append("m");
        }
        if (seconds > 0) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(seconds).append("s");
        }

        return sb.isEmpty() ? "0s" : sb.toString();
    }
}
