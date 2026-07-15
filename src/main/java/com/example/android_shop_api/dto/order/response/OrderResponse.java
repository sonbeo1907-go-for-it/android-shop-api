package com.example.android_shop_api.dto.order.response;

import com.example.android_shop_api.entity.order.OrderStatus;
import com.example.android_shop_api.entity.order.PaymentMethod;
import com.example.android_shop_api.entity.order.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        String orderCode,
        String receiverName,
        String phoneNumber,
        String email,
        String address,
        String note,
        List<OrderItemResponse> items,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal totalAmount,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        OrderStatus orderStatus,
        Instant createdAt
) {

    public OrderResponse {
        items = items == null
                ? List.of()
                : List.copyOf(items);
    }
}