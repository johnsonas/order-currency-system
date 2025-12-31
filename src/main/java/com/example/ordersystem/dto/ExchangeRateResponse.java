package com.example.ordersystem.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * ExchangeRate-API 回應 DTO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeRateResponse {
    
    @JsonProperty("base")
    private String base;
    
    @JsonProperty("date")
    private String date;
    
    @JsonProperty("rates")
    private Map<String, Double> rates;
    
    public String getBase() {
        return base;
    }
    
    public void setBase(String base) {
        this.base = base;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public Map<String, Double> getRates() {
        return rates;
    }
    
    public void setRates(Map<String, Double> rates) {
        this.rates = rates;
    }
}

