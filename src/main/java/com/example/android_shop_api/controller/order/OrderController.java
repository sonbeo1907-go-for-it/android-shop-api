package com.example.android_shop_api.controller.order;

import com.example.android_shop_api.dto.order.request.CreateOrderRequest;
import com.example.android_shop_api.dto.order.response.OrderResponse;
import com.example.android_shop_api.service.order.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@Validated
public class OrderController {

    private final OrderService orderService;

    public OrderController(
            OrderService orderService
    ) {
        this.orderService = orderService;
    }

    /*
     * POST /api/v1/orders
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(
            @Valid
            @RequestBody
            CreateOrderRequest request
    ) {
        return orderService.createOrder(request);
    }

    /*
     * GET /api/v1/orders/lookup
     *     ?orderCode=ORD-...
     *     &phoneNumber=090...
     */
    @GetMapping("/lookup")
    public OrderResponse lookupOrder(
            @RequestParam
            @NotBlank(
                    message = "Mã đơn hàng không được để trống"
            )
            @Size(
                    max = 40,
                    message = "Mã đơn hàng không hợp lệ"
            )
            String orderCode,

            @RequestParam
            @NotBlank(
                    message = "Số điện thoại không được để trống"
            )
            @Size(
                    max = 30,
                    message = "Số điện thoại không hợp lệ"
            )
            String phoneNumber
    ) {
        return orderService.lookupOrder(
                orderCode,
                phoneNumber
        );
    }
}