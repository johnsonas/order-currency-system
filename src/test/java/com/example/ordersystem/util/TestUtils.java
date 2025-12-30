package com.example.ordersystem.util;

import com.example.ordersystem.model.Currency;
import com.example.ordersystem.model.CurrencyCode;
import com.example.ordersystem.model.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 測試工具類
 * 提供通用的測試輔助方法
 * 
 * @author Order Currency System
 * @version 1.0
 */
public class TestUtils {
    
    /**
     * 建立一個測試用的 Order 物件
     */
    public static Order createTestOrder() {
        return OrderTestDataBuilder.createDefault();
    }
    
    /**
     * 建立一個測試用的 Order 物件（指定參數）
     */
    public static Order createTestOrder(Long orderId, String username, BigDecimal amount, CurrencyCode currency) {
        return OrderTestDataBuilder.builder()
            .withOrderId(orderId)
            .withUsername(username)
            .withAmount(amount)
            .withCurrency(currency)
            .build();
    }
    
    /**
     * 建立一個測試用的 Currency 物件
     */
    public static Currency createTestCurrency() {
        return CurrencyTestDataBuilder.createDefault();
    }
    
    /**
     * 建立一個測試用的 Currency 物件（指定參數）
     */
    public static Currency createTestCurrency(CurrencyCode currencyCode, BigDecimal rateToTwd) {
        return CurrencyTestDataBuilder.builder()
            .withCurrencyCode(currencyCode)
            .withRateToTwd(rateToTwd)
            .build();
    }
    
    /**
     * 比較兩個 Order 物件是否相等（忽略時間戳記）
     */
    public static void assertOrderEquals(Order expected, Order actual) {
        assertNotNull(actual, "Order should not be null");
        assertEquals(expected.getOrderId(), actual.getOrderId(), "Order ID should match");
        assertEquals(expected.getUsername(), actual.getUsername(), "Username should match");
        assertEquals(expected.getAmount(), actual.getAmount(), "Amount should match");
        assertEquals(expected.getCurrency(), actual.getCurrency(), "Currency should match");
        assertEquals(expected.getStatus(), actual.getStatus(), "Status should match");
        assertEquals(expected.getDiscount(), actual.getDiscount(), "Discount should match");
        assertEquals(expected.getFinalAmount(), actual.getFinalAmount(), "Final amount should match");
    }
    
    /**
     * 比較兩個 Order 物件是否相等（包含時間戳記）
     */
    public static void assertOrderEqualsWithTimestamps(Order expected, Order actual) {
        assertOrderEquals(expected, actual);
        assertEquals(expected.getCreatedAt(), actual.getCreatedAt(), "CreatedAt should match");
        assertEquals(expected.getUpdatedAt(), actual.getUpdatedAt(), "UpdatedAt should match");
    }
    
    /**
     * 比較兩個 Currency 物件是否相等
     */
    public static void assertCurrencyEquals(Currency expected, Currency actual) {
        assertNotNull(actual, "Currency should not be null");
        assertEquals(expected.getCurrencyCode(), actual.getCurrencyCode(), "Currency code should match");
        assertEquals(expected.getRateToTwd(), actual.getRateToTwd(), "Rate to TWD should match");
    }
    
    /**
     * 比較兩個 Currency 物件是否相等（包含時間戳記）
     */
    public static void assertCurrencyEqualsWithTimestamp(Currency expected, Currency actual) {
        assertCurrencyEquals(expected, actual);
        assertEquals(expected.getLastUpdate(), actual.getLastUpdate(), "Last update should match");
    }
    
    /**
     * 比較兩個 BigDecimal 是否相等（允許誤差）
     */
    public static void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual, BigDecimal delta) {
        assertNotNull(actual, "Actual BigDecimal should not be null");
        BigDecimal difference = expected.subtract(actual).abs();
        assertTrue(difference.compareTo(delta) <= 0, 
            String.format("Expected: %s, Actual: %s, Delta: %s", expected, actual, delta));
    }
    
    /**
     * 比較兩個 BigDecimal 是否相等（預設誤差 0.01）
     */
    public static void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        assertBigDecimalEquals(expected, actual, new BigDecimal("0.01"));
    }
    
    /**
     * 驗證 Order 的基本欄位不為 null
     */
    public static void assertOrderFieldsNotNull(Order order) {
        assertNotNull(order, "Order should not be null");
        assertNotNull(order.getUsername(), "Username should not be null");
        assertNotNull(order.getAmount(), "Amount should not be null");
        assertNotNull(order.getCurrency(), "Currency should not be null");
        assertNotNull(order.getStatus(), "Status should not be null");
    }
    
    /**
     * 驗證 Currency 的基本欄位不為 null
     */
    public static void assertCurrencyFieldsNotNull(Currency currency) {
        assertNotNull(currency, "Currency should not be null");
        assertNotNull(currency.getCurrencyCode(), "Currency code should not be null");
        assertNotNull(currency.getRateToTwd(), "Rate to TWD should not be null");
    }
    
    /**
     * 建立一個過去時間的 LocalDateTime（用於測試時間相關功能）
     */
    public static LocalDateTime createPastDateTime(int daysAgo) {
        return LocalDateTime.now().minusDays(daysAgo);
    }
    
    /**
     * 建立一個未來時間的 LocalDateTime（用於測試時間相關功能）
     */
    public static LocalDateTime createFutureDateTime(int daysLater) {
        return LocalDateTime.now().plusDays(daysLater);
    }
}



