package com.example.ordersystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "CURRENCIES")
public class Currency {
    
    @Id
    @NotNull(message = "幣別代碼不能為空")
    @Enumerated(EnumType.STRING)
    @Column(name = "CURRENCY_CODE", length = 3, nullable = false)
    private CurrencyCode currencyCode;
    
    @NotNull(message = "匯率不能為空")
    @Positive(message = "匯率必須大於0")
    @Column(name = "RATE_TO_TWD", nullable = false, precision = 19, scale = 6)
    private BigDecimal rateToTwd;
    
    @Column(name = "LAST_UPDATE")
    private LocalDateTime lastUpdate;
    
    @PrePersist
    protected void onCreate() {
        lastUpdate = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastUpdate = LocalDateTime.now();
    }
    
    // Getters and Setters
    public CurrencyCode getCurrencyCode() {
        return currencyCode;
    }
    
    public void setCurrencyCode(CurrencyCode currencyCode) {
        this.currencyCode = currencyCode;
    }
    
    public BigDecimal getRateToTwd() {
        return rateToTwd;
    }
    
    public void setRateToTwd(BigDecimal rateToTwd) {
        this.rateToTwd = rateToTwd;
    }
    
    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }
    
    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}


