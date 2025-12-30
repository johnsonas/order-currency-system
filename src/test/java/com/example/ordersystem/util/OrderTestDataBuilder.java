package com.example.ordersystem.util;

import com.example.ordersystem.model.CurrencyCode;
import com.example.ordersystem.model.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order 測試資料建構器
 * 使用 Builder Pattern 建立測試用的 Order 物件
 * 
 * @author Order Currency System
 * @version 1.0
 */
public class OrderTestDataBuilder {
    
    private Long orderId;
    private String username;
    private BigDecimal amount;
    private CurrencyCode currency;
    private String status;
    private BigDecimal discount;
    private BigDecimal finalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private OrderTestDataBuilder() {
        // 預設值
        this.status = "PENDING";
        this.discount = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public static OrderTestDataBuilder builder() {
        return new OrderTestDataBuilder();
    }
    
    public OrderTestDataBuilder withOrderId(Long orderId) {
        this.orderId = orderId;
        return this;
    }
    
    public OrderTestDataBuilder withUsername(String username) {
        this.username = username;
        return this;
    }
    
    public OrderTestDataBuilder withAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }
    
    public OrderTestDataBuilder withAmount(String amount) {
        this.amount = new BigDecimal(amount);
        return this;
    }
    
    public OrderTestDataBuilder withCurrency(CurrencyCode currency) {
        this.currency = currency;
        return this;
    }
    
    public OrderTestDataBuilder withStatus(String status) {
        this.status = status;
        return this;
    }
    
    public OrderTestDataBuilder withDiscount(BigDecimal discount) {
        this.discount = discount;
        return this;
    }
    
    public OrderTestDataBuilder withDiscount(String discount) {
        this.discount = new BigDecimal(discount);
        return this;
    }
    
    public OrderTestDataBuilder withFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
        return this;
    }
    
    public OrderTestDataBuilder withFinalAmount(String finalAmount) {
        this.finalAmount = new BigDecimal(finalAmount);
        return this;
    }
    
    public OrderTestDataBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }
    
    public OrderTestDataBuilder withUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }
    
    /**
     * 建立一個預設的測試訂單
     */
    public static Order createDefault() {
        return builder()
            .withUsername("testuser")
            .withAmount("1000.00")
            .withCurrency(CurrencyCode.USD)
            .build();
    }
    
    /**
     * 建立一個完整的測試訂單（包含所有欄位）
     */
    public static Order createFull() {
        return builder()
            .withOrderId(1L)
            .withUsername("testuser")
            .withAmount("1000.00")
            .withCurrency(CurrencyCode.USD)
            .withStatus("PENDING")
            .withDiscount("10.00")
            .withFinalAmount("900.00")
            .build();
    }
    
    /**
     * 建立訂單物件
     */
    public Order build() {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setUsername(username);
        order.setAmount(amount);
        order.setCurrency(currency);
        order.setStatus(status);
        order.setDiscount(discount);
        order.setFinalAmount(finalAmount);
        order.setCreatedAt(createdAt);
        order.setUpdatedAt(updatedAt);
        return order;
    }
}



