package com.example.android_shop_api.dto.phone.response;

import java.math.BigDecimal;

public record PhoneCardResponse(
        Long id,
        String name,
        String slug,
        String model,
        String brand,
        BigDecimal basePrice,
        BigDecimal originalPrice,
        String thumbnailUrl,
        Integer stockQuantity,
        Long soldCount,
        boolean featured
) {
}