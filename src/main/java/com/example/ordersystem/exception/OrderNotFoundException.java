package com.example.ordersystem.exception;

/**
 * 訂單不存在異常
 */
public class OrderNotFoundException extends ResourceNotFoundException {
    
    public OrderNotFoundException(Long orderId) {
        super("訂單", "orderId", orderId);
    }
    
    public OrderNotFoundException(String message) {
        super(message);
    }
}

