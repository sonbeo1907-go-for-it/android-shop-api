package com.example.android_shop_api.service.phone;

import com.example.android_shop_api.dto.common.PageResponse;
import com.example.android_shop_api.dto.phone.request.PhoneFilterRequest;
import com.example.android_shop_api.dto.phone.response.PhoneCardResponse;
import com.example.android_shop_api.dto.phone.response.PhoneDetailResponse;

import java.util.List;

public interface PhoneService {

    PageResponse<PhoneCardResponse> getPhones(
            PhoneFilterRequest request
    );

    PhoneDetailResponse getPhoneBySlug(
            String slug
    );

    List<PhoneCardResponse> getFeaturedPhones(
            int limit
    );

    List<PhoneCardResponse> getBestSellerPhones(
            int limit
    );
}