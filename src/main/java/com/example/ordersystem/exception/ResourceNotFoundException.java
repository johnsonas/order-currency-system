package com.example.ordersystem.exception;

/**
 * 資源不存在異常
 * 當請求的資源（如訂單、幣別）不存在時拋出此異常
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s 不存在: %s = '%s'", resourceName, fieldName, fieldValue));
    }
}

