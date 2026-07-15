package com.example.android_shop_api.dto.order.response;

import java.math.BigDecimal;
import java.util.List;

public record OrderItemResponse(
        Long phoneId,
        String phoneName,
        String imageUrl,
        BigDecimal basePrice,
        List<OrderItemOptionResponse> selectedOptions,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal totalPrice
) {

    public OrderItemResponse {
        selectedOptions = selectedOptions == null
                ? List.of()
                : List.copyOf(selectedOptions);
    }
}