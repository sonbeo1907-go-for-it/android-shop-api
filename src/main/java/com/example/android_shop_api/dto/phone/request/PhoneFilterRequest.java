package com.example.android_shop_api.dto.phone.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PhoneFilterRequest {

    private String keyword;

    private String brand;

    @PositiveOrZero(
            message = "Giá tối thiểu không được nhỏ hơn 0"
    )
    private BigDecimal minPrice;

    @PositiveOrZero(
            message = "Giá tối đa không được nhỏ hơn 0"
    )
    private BigDecimal maxPrice;

    /*
     * Các giá trị dự kiến:
     * price
     * soldCount
     * createdAt
     * name
     */
    private String sortBy = "createdAt";

    /*
     * asc hoặc desc
     */
    private String sortDirection = "desc";

    @Min(
            value = 0,
            message = "Số trang không được nhỏ hơn 0"
    )
    private int page = 0;

    @Min(
            value = 1,
            message = "Kích thước trang phải từ 1 trở lên"
    )
    @Max(
            value = 50,
            message = "Kích thước trang không được vượt quá 50"
    )
    private int size = 12;
}