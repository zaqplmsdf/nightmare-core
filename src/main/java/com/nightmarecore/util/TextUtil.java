package com.nightmarecore.util;

import java.util.Locale;

public final class TextUtil {

    public static String number(double value) {
        if (value == Math.rint(value)) {
            return Long.toString((long) value);
        }
        String formatted = String.format(Locale.ROOT, "%.2f", value);
        int end = formatted.length();
        while (end > 0 && formatted.charAt(end - 1) == '0') {
            end--;
        }
        if (end > 0 && formatted.charAt(end - 1) == '.') {
            end--;
        }
        return formatted.substring(0, end);
    }

    private TextUtil() {
    }
}
