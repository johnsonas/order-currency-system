package com.example.ordersystem.service;

import com.example.ordersystem.model.Currency;
import com.example.ordersystem.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CurrencyService {
    
    @Autowired
    private CurrencyRepository currencyRepository;
    
    public List<Currency> getAllCurrencies() {
        return currencyRepository.findAll();
    }
    
    public Optional<Currency> getCurrencyByCode(String currencyCode) {
        return currencyRepository.findByCurrencyCode(currencyCode);
    }
    
    public Currency createOrUpdateCurrency(Currency currency) {
        return currencyRepository.save(currency);
    }
    
    public void deleteCurrency(String currencyCode) {
        currencyRepository.deleteById(currencyCode);
    }
    
    // 將金額轉換為 TWD
    public BigDecimal convertToTwd(BigDecimal amount, String sourceCurrency) {
        if (sourceCurrency.equals("TWD")) {
            return amount;
        }
        
        Optional<Currency> currencyOpt = currencyRepository.findByCurrencyCode(sourceCurrency);
        if (currencyOpt.isPresent()) {
            Currency currency = currencyOpt.get();
            // 故意留一個小 bug：沒有處理匯率為 null 的情況
            return amount.multiply(currency.getRateToTwd()).setScale(2, RoundingMode.HALF_UP);
        }
        throw new RuntimeException("找不到幣別: " + sourceCurrency);
    }
    
    // 幣別轉換（從 sourceCurrency 轉換為 targetCurrency）
    public BigDecimal convertCurrency(BigDecimal amount, String sourceCurrency, String targetCurrency) {
        if (sourceCurrency.equals(targetCurrency)) {
            return amount;
        }
        
        // 先轉換為 TWD，再轉換為目標幣別
        BigDecimal twdAmount = convertToTwd(amount, sourceCurrency);
        
        if (targetCurrency.equals("TWD")) {
            return twdAmount;
        }
        
        Optional<Currency> targetCurrencyOpt = currencyRepository.findByCurrencyCode(targetCurrency);
        if (targetCurrencyOpt.isPresent()) {
            Currency currency = targetCurrencyOpt.get();
            // 故意留一個不優化的地方：除法運算可能會有精度問題
            return twdAmount.divide(currency.getRateToTwd(), 2, RoundingMode.HALF_UP);
        }
        throw new RuntimeException("找不到幣別: " + targetCurrency);
    }
    
    // 更新匯率
    public Currency updateRate(String currencyCode, BigDecimal newRate) {
        Optional<Currency> currencyOpt = currencyRepository.findByCurrencyCode(currencyCode);
        if (currencyOpt.isPresent()) {
            Currency currency = currencyOpt.get();
            currency.setRateToTwd(newRate);
            return currencyRepository.save(currency);
        }
        throw new RuntimeException("找不到幣別: " + currencyCode);
    }
}

