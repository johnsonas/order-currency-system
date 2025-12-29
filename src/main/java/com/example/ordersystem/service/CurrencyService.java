package com.example.ordersystem.service;

import com.example.ordersystem.model.Currency;
import com.example.ordersystem.model.CurrencyCode;
import com.example.ordersystem.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * 幣別服務類
 * 提供幣別相關的業務邏輯處理，包括幣別的 CRUD 操作、匯率轉換等功能
 * 
 * @author Order Currency System
 * @version 1.0
 */
@Service
@Transactional
public class CurrencyService {
    
    @Autowired
    private CurrencyRepository currencyRepository;
    
    /**
     * 取得所有幣別列表
     * 
     * @return 所有幣別的列表
     */
    public List<Currency> getAllCurrencies() {
        return currencyRepository.findAll();
    }
    
    /**
     * 根據幣別代碼取得幣別資訊
     * 
     * @param currencyCode 幣別代碼 Enum（如：USD, EUR, JPY, TWD）
     * @return 幣別的 Optional 物件，如果不存在則返回空 Optional
     */
    public Optional<Currency> getCurrencyByCode(CurrencyCode currencyCode) {
        return currencyRepository.findByCurrencyCode(currencyCode);
    }
    
    /**
     * 建立或更新幣別資訊
     * 如果幣別代碼已存在則更新，不存在則建立
     * 
     * @param currency 幣別物件
     * @return 儲存後的幣別物件
     */
    public Currency createOrUpdateCurrency(Currency currency) {
        return currencyRepository.save(currency);
    }
    
    /**
     * 刪除幣別
     * 
     * @param currencyCode 要刪除的幣別代碼 Enum
     */
    public void deleteCurrency(CurrencyCode currencyCode) {
        currencyRepository.deleteById(currencyCode);
    }
    
    /**
     * 將指定金額轉換為新台幣（TWD）
     * 如果來源幣別已經是 TWD，則直接返回原金額
     * 
     * @param amount 要轉換的金額
     * @param sourceCurrency 來源幣別代碼 Enum
     * @return 轉換後的新台幣金額（保留2位小數）
     * @throws RuntimeException 如果找不到指定的幣別
     * @apiNote 故意留一個小 bug：沒有處理匯率為 null 的情況
     */
    public BigDecimal convertToTwd(BigDecimal amount, CurrencyCode sourceCurrency) {
        if (sourceCurrency == CurrencyCode.TWD) {
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
    
    /**
     * 將金額從來源幣別轉換為目標幣別
     * 轉換流程：來源幣別 → TWD → 目標幣別
     * 如果來源幣別和目標幣別相同，則直接返回原金額
     * 
     * @param amount 要轉換的金額
     * @param sourceCurrency 來源幣別代碼 Enum
     * @param targetCurrency 目標幣別代碼 Enum
     * @return 轉換後的目標幣別金額（保留2位小數）
     * @throws RuntimeException 如果找不到指定的幣別
     * @apiNote 故意留一個不優化的地方：除法運算可能會有精度問題
     */
    public BigDecimal convertCurrency(BigDecimal amount, CurrencyCode sourceCurrency, CurrencyCode targetCurrency) {
        if (sourceCurrency == targetCurrency) {
            return amount;
        }
        
        // 先轉換為 TWD，再轉換為目標幣別
        BigDecimal twdAmount = convertToTwd(amount, sourceCurrency);
        
        if (targetCurrency == CurrencyCode.TWD) {
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
    
    /**
     * 更新指定幣別的匯率
     * 只更新匯率，不更新其他欄位
     * 
     * @param currencyCode 幣別代碼 Enum
     * @param newRate 新的匯率（相對於 TWD 的匯率）
     * @return 更新後的幣別物件
     * @throws RuntimeException 如果找不到指定的幣別
     */
    public Currency updateRate(CurrencyCode currencyCode, BigDecimal newRate) {
        Optional<Currency> currencyOpt = currencyRepository.findByCurrencyCode(currencyCode);
        if (currencyOpt.isPresent()) {
            Currency currency = currencyOpt.get();
            currency.setRateToTwd(newRate);
            return currencyRepository.save(currency);
        }
        throw new RuntimeException("找不到幣別: " + currencyCode);
    }
}

