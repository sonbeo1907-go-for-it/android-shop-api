package com.example.android_shop_api.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class OrderCodeGenerator {

    private static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    /*
     * Loại bỏ các ký tự dễ nhầm:
     * O, 0, I, 1
     */
    private static final char[] ALPHABET =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
                    .toCharArray();

    private static final int RANDOM_LENGTH = 6;

    private final SecureRandom secureRandom =
            new SecureRandom();

    public String generate() {
        String datePart = LocalDate
                .now(BUSINESS_ZONE)
                .format(DATE_FORMATTER);

        StringBuilder randomPart =
                new StringBuilder(RANDOM_LENGTH);

        for (int index = 0;
             index < RANDOM_LENGTH;
             index++) {

            int randomIndex = secureRandom.nextInt(
                    ALPHABET.length
            );

            randomPart.append(
                    ALPHABET[randomIndex]
            );
        }

        return "ORD-"
                + datePart
                + "-"
                + randomPart;
    }
}