package com.example.android_shop_api.service.order;

import com.example.android_shop_api.dto.order.request.CreateOrderRequest;
import com.example.android_shop_api.dto.order.response.OrderResponse;

public interface OrderService {

    OrderResponse createOrder(
            CreateOrderRequest request
    );

    OrderResponse lookupOrder(
            String orderCode,
            String phoneNumber
    );
}