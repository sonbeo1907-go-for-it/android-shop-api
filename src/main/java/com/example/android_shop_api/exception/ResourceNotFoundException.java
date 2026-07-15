package com.example.android_shop_api.exception;

import com.example.android_shop_api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(
            String code,
            String message
    ) {
        super(code, message, HttpStatus.NOT_FOUND);
    }
}