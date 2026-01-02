package com.example.ordersystem.service;

import com.example.ordersystem.exception.CurrencyNotFoundException;
import com.example.ordersystem.model.Currency;
import com.example.ordersystem.model.CurrencyCode;
import com.example.ordersystem.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 幣別匯率服務類
 * 提供幣別查詢、新增、更新、刪除及匯率換算等業務邏輯處理
 * 
 * 本服務用於管理系統支援的各種幣別與兌台幣（TWD）的匯率，
 * 並支援金額的幣別轉換功能。
 * 
 * @author Order Currency System
 * @version 1.0
 */

@Service
@Transactional
public class CurrencyService {
    
    private static final Logger logger = LoggerFactory.getLogger(CurrencyService.class);
    private static final String REDIS_KEY_PREFIX = "currency:rate:";
    private static final Duration CACHE_TTL = Duration.ofHours(24); // 快取 24 小時
    
    @Autowired
    private CurrencyRepository currencyRepository;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    /**
     * 取得所有幣別列表
     * 
     * @return 所有幣別的列表，如果為 null 則返回空列表
     */
    public List<Currency> getAllCurrencies() {
        List<Currency> currencies = currencyRepository.findAll();
        return currencies != null ? currencies : Collections.emptyList();
    }
    
    /**
     * 根據幣別代碼取得幣別資訊
     * 優先從 Redis 快取讀取，如果快取不存在則從資料庫查詢並寫入快取
     * 
     * @param currencyCode 幣別代碼 Enum（如：USD, EUR, JPY, TWD）
     * @return 幣別的 Optional 物件，如果不存在則返回空 Optional
     */
    public Optional<Currency> getCurrencyByCode(CurrencyCode currencyCode) {
        String cacheKey = REDIS_KEY_PREFIX + currencyCode.name();
        
        // 先從 Redis 快取讀取
        logger.debug("嘗試從 Redis 讀取幣別快取: {}", cacheKey);
        Currency cachedCurrency = (Currency) redisTemplate.opsForValue().get(cacheKey);
        if (cachedCurrency != null) {
            logger.debug("Redis 快取命中: {} = {}", cacheKey, cachedCurrency.getRateToTwd());
            return Optional.of(cachedCurrency);
        }
        
        logger.debug("Redis 快取未命中，從資料庫查詢: {}", currencyCode);
        // 快取不存在，從資料庫查詢
        Optional<Currency> currencyOpt = currencyRepository.findByCurrencyCode(currencyCode);
        if (currencyOpt.isPresent()) {
            Currency currency = currencyOpt.get();
            logger.debug("從資料庫取得幣別: {} = {}", currencyCode, currency.getRateToTwd());
            // 寫入 Redis 快取
            logger.info("寫入 Redis 快取: {} = {}, TTL: {} 小時", cacheKey, currency.getRateToTwd(), CACHE_TTL.toHours());
            redisTemplate.opsForValue().set(cacheKey, currency, CACHE_TTL);
            logger.debug("Redis 快取寫入完成: {}", cacheKey);
            return Optional.of(currency);
        }
        
        logger.debug("資料庫中找不到幣別: {}", currencyCode);
        return Optional.empty();
    }
    




    /**
     * 建立或更新幣別資訊
     * 如果幣別代碼已存在則更新，不存在則建立
     * 更新後會同步更新 Redis 快取
     * 
     * @param currency 幣別物件
     * @return 儲存後的幣別物件
     */
    public Currency createOrUpdateCurrency(Currency currency) {
        logger.info("=== 開始儲存幣別到資料庫 ===");
        logger.info("幣別代碼: {}, 匯率: {}", currency.getCurrencyCode(), currency.getRateToTwd());
        
        Currency savedCurrency = currencyRepository.save(currency);
        logger.info("幣別已存入資料庫: {}", savedCurrency.getCurrencyCode());
        
        // 更新 Redis 快取
        updateCache(savedCurrency);
        
        logger.info("=== 幣別儲存完成 ===");
        return savedCurrency;
    }
    
    /**
     * 刪除幣別
     * 同時清除 Redis 快取
     * 
     * @param currencyCode 要刪除的幣別代碼 Enum
     */
    public void deleteCurrency(CurrencyCode currencyCode) {
        currencyRepository.deleteById(currencyCode);
        // 清除 Redis 快取
        String cacheKey = REDIS_KEY_PREFIX + currencyCode.name();
        redisTemplate.delete(cacheKey);
    }
    
    /**
     * 將指定金額轉換為新台幣（TWD）
     * 如果來源幣別已經是 TWD，則直接返回原金額
     * 
     * @param amount 要轉換的金額
     * @param sourceCurrency 來源幣別代碼 Enum
     * @return 轉換後的新台幣金額（保留2位小數）
     * @throws CurrencyNotFoundException 如果找不到指定的幣別
     * @apiNote 故意留一個小 bug：沒有處理匯率為 null 的情況
     */
    public BigDecimal convertToTwd(BigDecimal amount, CurrencyCode sourceCurrency) {
        if (sourceCurrency == CurrencyCode.TWD) {
            return amount;
        }
        
        // 使用 getCurrencyByCode 方法以利用 Redis 快取
        Optional<Currency> currencyOpt = getCurrencyByCode(sourceCurrency);
        if (currencyOpt.isPresent()) {
            Currency currency = currencyOpt.get();
            // 故意留一個小 bug：沒有處理匯率為 null 的情況
            return amount.multiply(currency.getRateToTwd()).setScale(2, RoundingMode.HALF_UP);
        }
        throw new CurrencyNotFoundException(sourceCurrency);
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
     * @throws CurrencyNotFoundException 如果找不到指定的幣別
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
        
        // 使用 getCurrencyByCode 方法以利用 Redis 快取
        Optional<Currency> targetCurrencyOpt = getCurrencyByCode(targetCurrency);
        if (targetCurrencyOpt.isPresent()) {
            Currency currency = targetCurrencyOpt.get();
            // 故意留一個不優化的地方：除法運算可能會有精度問題
            return twdAmount.divide(currency.getRateToTwd(), 2, RoundingMode.HALF_UP);
        }
        throw new CurrencyNotFoundException(targetCurrency);
    }
    
    /**
     * 更新指定幣別的匯率
     * 只更新匯率，不更新其他欄位
     * 更新後會清除並重新寫入 Redis 快取
     * 
     * @param currencyCode 幣別代碼 Enum
     * @param newRate 新的匯率（相對於 TWD 的匯率）
     * @return 更新後的幣別物件
     * @throws CurrencyNotFoundException 如果找不到指定的幣別
     */
    public Currency updateRate(CurrencyCode currencyCode, BigDecimal newRate) {
        logger.info("=== 開始更新匯率 ===");
        logger.info("幣別代碼: {}, 新匯率: {}", currencyCode, newRate);
        
        // 使用 getCurrencyByCode 方法以利用 Redis 快取
        Optional<Currency> currencyOpt = getCurrencyByCode(currencyCode);
        if (currencyOpt.isPresent()) {
            Currency currency = currencyOpt.get();
            BigDecimal oldRate = currency.getRateToTwd();
            logger.info("舊匯率: {}, 新匯率: {}", oldRate, newRate);
            
            currency.setRateToTwd(newRate);
            logger.info("正在更新資料庫中的匯率...");
            Currency savedCurrency = currencyRepository.save(currency);
            logger.info("資料庫更新完成: {} = {}", currencyCode, savedCurrency.getRateToTwd());
            
            // 更新 Redis 快取
            updateCache(savedCurrency);
            
            logger.info("=== 匯率更新完成 ===");
            return savedCurrency;
        }
        logger.error("找不到幣別: {}", currencyCode);
        throw new CurrencyNotFoundException(currencyCode);
    }
    
    /**
     * 更新 Redis 快取
     * 
     * @param currency 要快取的幣別物件
     */
    private void updateCache(Currency currency) {
        String cacheKey = REDIS_KEY_PREFIX + currency.getCurrencyCode().name();
        logger.info("=== 開始更新 Redis 快取 ===");
        logger.info("快取鍵: {}", cacheKey);
        logger.info("幣別資料: currencyCode={}, rateToTwd={}, lastUpdate={}", 
            currency.getCurrencyCode(), currency.getRateToTwd(), currency.getLastUpdate());
        
        redisTemplate.opsForValue().set(cacheKey, currency, CACHE_TTL);
        
        logger.info("Redis 快取更新完成: {} (TTL: {} 小時)", cacheKey, CACHE_TTL.toHours());
        logger.info("=== Redis 快取更新完成 ===");
    }
    
    /**
     * 清除指定幣別的 Redis 快取
     * 當資料庫資料被直接修改時，可以手動清除快取以強制重新從資料庫讀取
     * 
     * @param currencyCode 幣別代碼 Enum
     */
    public void evictCache(CurrencyCode currencyCode) {
        String cacheKey = REDIS_KEY_PREFIX + currencyCode.name();
        redisTemplate.delete(cacheKey);
    }
    
    /**
     * 清除所有幣別的 Redis 快取
     * 當資料庫資料被大量修改時，可以清除所有快取
     */
    public void evictAllCache() {
        // 使用 Redis 的 KEYS 命令找到所有相關的快取鍵
        String pattern = REDIS_KEY_PREFIX + "*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

   



    
}

