package com.example.android_shop_api.entity.order;

import com.example.android_shop_api.entity.phone.PhoneOptionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemOptionSnapshot {

    private Long optionId;

    private PhoneOptionType type;

    private String value;

    private BigDecimal extraPrice;
}