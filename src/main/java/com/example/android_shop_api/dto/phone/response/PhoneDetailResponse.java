package com.example.android_shop_api.dto.phone.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record PhoneDetailResponse(
        Long id,
        String name,
        String slug,
        String model,
        String brand,
        BigDecimal basePrice,
        BigDecimal originalPrice,
        String shortDescription,
        String description,
        String thumbnailUrl,
        List<String> images,
        Map<String, String> specifications,
        Integer stockQuantity,
        Long soldCount,
        boolean featured,
        List<PhoneOptionGroupResponse> optionGroups,
        Instant createdAt,
        Instant updatedAt
) {

    public PhoneDetailResponse {
        images = images == null
                ? List.of()
                : List.copyOf(images);

        specifications = specifications == null
                ? Map.of()
                : Map.copyOf(specifications);

        optionGroups = optionGroups == null
                ? List.of()
                : List.copyOf(optionGroups);
    }
}