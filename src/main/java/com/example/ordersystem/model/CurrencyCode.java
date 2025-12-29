package com.example.ordersystem.model;

/**
 * 幣別代碼枚舉
 * 定義系統支援的所有幣別
 * 
 * @author Order Currency System
 * @version 1.0
 */
public enum CurrencyCode {
    /** 新台幣 */
    TWD("TWD"),
    /** 美元 */
    USD("USD"),
    /** 歐元 */
    EUR("EUR"),
    /** 日圓 */
    JPY("JPY"),
    /** 人民幣 */
    CNY("CNY");
    
    private final String code;
    
    CurrencyCode(String code) {
        this.code = code;
    }
    
    /**
     * 取得幣別代碼字串
     * 
     * @return 幣別代碼字串（如：TWD, USD, EUR）
     */
    public String getCode() {
        return code;
    }
    
    /**
     * 根據字串代碼取得對應的 CurrencyCode Enum
     * 
     * @param code 幣別代碼字串
     * @return 對應的 CurrencyCode Enum，如果找不到則返回 null
     */
    public static CurrencyCode fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (CurrencyCode currencyCode : CurrencyCode.values()) {
            if (currencyCode.code.equalsIgnoreCase(code)) {
                return currencyCode;
            }
        }
        return null;
    }
    
    /**
     * 檢查字串是否為有效的幣別代碼
     * 
     * @param code 幣別代碼字串
     * @return 如果為有效幣別代碼則返回 true，否則返回 false
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }
}

