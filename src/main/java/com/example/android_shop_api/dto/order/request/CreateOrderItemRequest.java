package com.example.android_shop_api.dto.order.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateOrderItemRequest {

    @NotNull(
            message = "ID điện thoại không được để trống"
    )
    private Long phoneId;

    /*
     * Business layer ở phần 2 sẽ kiểm tra:
     * - option tồn tại
     * - option thuộc Phone
     * - không trùng loại
     * - đủ COLOR, RAM và STORAGE
     */
    @NotEmpty(
            message = "Phải chọn option cho điện thoại"
    )
    @Size(
            max = 10,
            message = "Số lượng option không được vượt quá 10"
    )
    private List<
            @NotNull(message = "ID option không được để trống")
                    Long
            > optionIds;

    @NotNull(
            message = "Số lượng không được để trống"
    )
    @Min(
            value = 1,
            message = "Số lượng phải từ 1 trở lên"
    )
    @Max(
            value = 10,
            message = "Mỗi sản phẩm chỉ được mua tối đa 10 chiếc"
    )
    private Integer quantity;
}