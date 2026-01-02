package com.example.ordersystem.exception;

import com.example.ordersystem.model.CurrencyCode;

/**
 * 幣別不存在異常
 */
public class CurrencyNotFoundException extends ResourceNotFoundException {
    
    public CurrencyNotFoundException(CurrencyCode currencyCode) {
        super("幣別", "currencyCode", currencyCode);
    }
    
    public CurrencyNotFoundException(String message) {
        super(message);
    }
}

