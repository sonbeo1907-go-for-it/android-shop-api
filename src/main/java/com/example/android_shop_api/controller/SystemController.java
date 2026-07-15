package com.example.android_shop_api.controller;

import com.example.android_shop_api.exception.ResourceNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of(
                "status", "UP",
                "service", "android-shop-api",
                "timestamp", Instant.now()
        );
    }

    @GetMapping("/test-error")
    public Map<String, Object> testError(
            @RequestParam(defaultValue = "false") boolean enabled
    ) {
        if (enabled) {
            throw new ResourceNotFoundException(
                    "TEST_RESOURCE_NOT_FOUND",
                    "Đây là lỗi dùng để kiểm tra GlobalExceptionHandler"
            );
        }

        return Map.of("errorTriggered", false);
    }
}