package com.example.ordersystem.exception;

/**
 * 請求參數錯誤異常
 * 當請求參數不符合要求時拋出此異常
 */
public class BadRequestException extends RuntimeException {
    
    public BadRequestException(String message) {
        super(message);
    }
    
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}

