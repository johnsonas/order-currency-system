package com.example.ordersystem.util;

import com.example.ordersystem.model.Currency;
import com.example.ordersystem.model.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Currency 測試資料建構器
 * 使用 Builder Pattern 建立測試用的 Currency 物件
 * 
 * @author Order Currency System
 * @version 1.0
 */
public class CurrencyTestDataBuilder {
    
    private CurrencyCode currencyCode;
    private BigDecimal rateToTwd;
    private LocalDateTime lastUpdate;
    
    private CurrencyTestDataBuilder() {
        this.lastUpdate = LocalDateTime.now();
    }
    
    public static CurrencyTestDataBuilder builder() {
        return new CurrencyTestDataBuilder();
    }
    
    public CurrencyTestDataBuilder withCurrencyCode(CurrencyCode currencyCode) {
        this.currencyCode = currencyCode;
        return this;
    }
    
    public CurrencyTestDataBuilder withRateToTwd(BigDecimal rateToTwd) {
        this.rateToTwd = rateToTwd;
        return this;
    }
    
    public CurrencyTestDataBuilder withRateToTwd(String rateToTwd) {
        this.rateToTwd = new BigDecimal(rateToTwd);
        return this;
    }
    
    public CurrencyTestDataBuilder withLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
        return this;
    }
    
    /**
     * 建立一個預設的測試幣別（USD）
     */
    public static Currency createDefault() {
        return builder()
            .withCurrencyCode(CurrencyCode.USD)
            .withRateToTwd("31.250000")
            .build();
    }
    
    /**
     * 建立 TWD 幣別
     */
    public static Currency createTwd() {
        return builder()
            .withCurrencyCode(CurrencyCode.TWD)
            .withRateToTwd("1.000000")
            .build();
    }
    
    /**
     * 建立 USD 幣別
     */
    public static Currency createUsd() {
        return builder()
            .withCurrencyCode(CurrencyCode.USD)
            .withRateToTwd("31.250000")
            .build();
    }
    
    /**
     * 建立 EUR 幣別
     */
    public static Currency createEur() {
        return builder()
            .withCurrencyCode(CurrencyCode.EUR)
            .withRateToTwd("34.480000")
            .build();
    }
    
    /**
     * 建立 JPY 幣別
     */
    public static Currency createJpy() {
        return builder()
            .withCurrencyCode(CurrencyCode.JPY)
            .withRateToTwd("0.220000")
            .build();
    }
    
    /**
     * 建立 CNY 幣別
     */
    public static Currency createCny() {
        return builder()
            .withCurrencyCode(CurrencyCode.CNY)
            .withRateToTwd("4.350000")
            .build();
    }
    
    /**
     * 建立幣別物件
     */
    public Currency build() {
        Currency currency = new Currency();
        currency.setCurrencyCode(currencyCode);
        currency.setRateToTwd(rateToTwd);
        currency.setLastUpdate(lastUpdate);
        return currency;
    }
}



