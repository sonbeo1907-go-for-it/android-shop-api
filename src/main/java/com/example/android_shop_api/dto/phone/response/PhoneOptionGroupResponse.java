package com.example.android_shop_api.dto.phone.response;

import com.example.android_shop_api.entity.phone.PhoneOptionType;

import java.util.List;

public record PhoneOptionGroupResponse(
        PhoneOptionType type,
        List<PhoneOptionResponse> values
) {

    public PhoneOptionGroupResponse {
        values = values == null
                ? List.of()
                : List.copyOf(values);
    }
}