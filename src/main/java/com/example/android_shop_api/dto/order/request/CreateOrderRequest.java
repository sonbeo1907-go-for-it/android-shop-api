package com.example.android_shop_api.dto.order.request;

import com.example.android_shop_api.entity.order.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateOrderRequest {

    @NotBlank(
            message = "Tên người nhận không được để trống"
    )
    @Size(
            max = 120,
            message = "Tên người nhận không được vượt quá 120 ký tự"
    )
    private String receiverName;

    @NotBlank(
            message = "Số điện thoại không được để trống"
    )
    @Size(
            max = 20,
            message = "Số điện thoại không được vượt quá 20 ký tự"
    )
    private String phoneNumber;

    @Email(
            message = "Email không đúng định dạng"
    )
    @Size(
            max = 160,
            message = "Email không được vượt quá 160 ký tự"
    )
    private String email;

    @NotBlank(
            message = "Địa chỉ nhận hàng không được để trống"
    )
    @Size(
            max = 500,
            message = "Địa chỉ không được vượt quá 500 ký tự"
    )
    private String address;

    @Size(
            max = 1000,
            message = "Ghi chú không được vượt quá 1000 ký tự"
    )
    private String note;

    @NotNull(
            message = "Phương thức thanh toán không được để trống"
    )
    private PaymentMethod paymentMethod;

    @Valid
    @NotEmpty(
            message = "Đơn hàng phải có ít nhất một sản phẩm"
    )
    @Size(
            max = 20,
            message = "Đơn hàng không được vượt quá 20 dòng sản phẩm"
    )
    private List<CreateOrderItemRequest> items;
}