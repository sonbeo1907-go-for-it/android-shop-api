package com.example.android_shop_api.controller.phone;

import com.example.android_shop_api.dto.common.PageResponse;
import com.example.android_shop_api.dto.phone.request.PhoneFilterRequest;
import com.example.android_shop_api.dto.phone.response.PhoneCardResponse;
import com.example.android_shop_api.dto.phone.response.PhoneDetailResponse;
import com.example.android_shop_api.service.phone.PhoneService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/phones")
public class PhoneController {

    private final PhoneService phoneService;

    public PhoneController(
            PhoneService phoneService
    ) {
        this.phoneService = phoneService;
    }

    /*
     * GET /api/v1/phones
     */
    @GetMapping
    public PageResponse<PhoneCardResponse> getPhones(
            @Valid
            @ModelAttribute
            PhoneFilterRequest request
    ) {
        return phoneService.getPhones(request);
    }

    /*
     * GET /api/v1/phones/slug/{slug}
     */
    @GetMapping("/slug/{slug}")
    public PhoneDetailResponse getPhoneBySlug(
            @PathVariable String slug
    ) {
        return phoneService.getPhoneBySlug(slug);
    }

    /*
     * GET /api/v1/phones/featured?limit=8
     */
    @GetMapping("/featured")
    public List<PhoneCardResponse> getFeaturedPhones(
            @RequestParam(defaultValue = "8")
            int limit
    ) {
        return phoneService.getFeaturedPhones(limit);
    }

    /*
     * GET /api/v1/phones/best-sellers?limit=8
     */
    @GetMapping("/best-sellers")
    public List<PhoneCardResponse> getBestSellerPhones(
            @RequestParam(defaultValue = "8")
            int limit
    ) {
        return phoneService.getBestSellerPhones(limit);
    }
}