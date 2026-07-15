package com.example.android_shop_api.dto.order.response;

import com.example.android_shop_api.entity.phone.PhoneOptionType;

import java.math.BigDecimal;

public record OrderItemOptionResponse(
        Long optionId,
        PhoneOptionType type,
        String value,
        BigDecimal extraPrice
) {
}