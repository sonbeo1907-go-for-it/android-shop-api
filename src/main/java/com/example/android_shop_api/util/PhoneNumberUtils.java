package com.example.android_shop_api.util;

import java.util.regex.Pattern;

public final class PhoneNumberUtils {

    private static final Pattern VIETNAMESE_MOBILE_PATTERN =
            Pattern.compile("^0[35789]\\d{8}$");

    private PhoneNumberUtils() {
    }

    public static String normalize(String rawPhoneNumber) {
        if (rawPhoneNumber == null) {
            return null;
        }

        String normalized = rawPhoneNumber
                .trim()
                .replaceAll("[\\s().-]", "");

        if (normalized.startsWith("+84")) {
            normalized = "0" + normalized.substring(3);
        } else if (normalized.startsWith("0084")) {
            normalized = "0" + normalized.substring(4);
        } else if (
                normalized.startsWith("84")
                        && normalized.length() == 11
        ) {
            normalized = "0" + normalized.substring(2);
        }

        return normalized;
    }

    public static boolean isValidVietnameseMobile(
            String phoneNumber
    ) {
        if (phoneNumber == null) {
            return false;
        }

        return VIETNAMESE_MOBILE_PATTERN
                .matcher(phoneNumber)
                .matches();
    }
}