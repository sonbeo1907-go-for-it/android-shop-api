package com.example.android_shop_api.dto.phone.response;

import com.example.android_shop_api.entity.phone.PhoneOptionType;

import java.math.BigDecimal;

public record PhoneOptionResponse(
        Long id,
        PhoneOptionType type,
        String value,
        BigDecimal extraPrice,
        String imageUrl,
        int displayOrder
) {
}