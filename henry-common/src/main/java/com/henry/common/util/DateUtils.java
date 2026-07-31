package com.henry.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间工具
 */
public final class DateUtils {

    public static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_PATTERN);

    private DateUtils() {
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? null : DEFAULT_FORMATTER.format(dateTime);
    }

    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDateTime parse(String text) {
        return parse(text, DEFAULT_PATTERN);
    }

    public static LocalDateTime parse(String text, String pattern) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(text, DateTimeFormatter.ofPattern(pattern));
    }
}
