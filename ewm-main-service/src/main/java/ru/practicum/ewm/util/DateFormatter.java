package ru.practicum.ewm.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class DateFormatter {

    private static final DateTimeFormatter dtFormatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneOffset.UTC);

    public static String dateToString(LocalDateTime date) {
        return date != null ? dtFormatter.format(date) : null;
    }

    public static LocalDateTime stringToDate(String date) {
        return date != null ? LocalDateTime.parse(date, dtFormatter) : null;
    }
}
