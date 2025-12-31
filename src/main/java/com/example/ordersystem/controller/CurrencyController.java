package com.example.ordersystem.controller;

import com.example.ordersystem.model.Currency;
import com.example.ordersystem.model.CurrencyCode;
import com.example.ordersystem.scheduler.CurrencyRateUpdateScheduler;
import com.example.ordersystem.service.CurrencyService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {
    
    private static final Logger logger = LoggerFactory.getLogger(CurrencyController.class);
    
    @Autowired
    private CurrencyService currencyService;
    
    @Autowired
    private CurrencyRateUpdateScheduler currencyRateUpdateScheduler;
    
    @GetMapping
    public ResponseEntity<List<Currency>> getAllCurrencies() {
        List<Currency> currencies = currencyService.getAllCurrencies();
        return ResponseEntity.ok(currencies);
    }
    
    @GetMapping("/{code}")
    public ResponseEntity<Currency> getCurrencyByCode(@PathVariable String code) {
        CurrencyCode currencyCode = CurrencyCode.fromCode(code);
        if (currencyCode == null) {
            return ResponseEntity.badRequest().build();
        }
        Optional<Currency> currency = currencyService.getCurrencyByCode(currencyCode);
        return currency.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Currency> createCurrency(@Valid @RequestBody Currency currency) {
        Currency createdCurrency = currencyService.createOrUpdateCurrency(currency);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCurrency);
    }
    
    @PutMapping("/{code}")
    public ResponseEntity<Currency> updateCurrency(
            @PathVariable String code,
            @Valid @RequestBody Currency currency) {
        CurrencyCode currencyCode = CurrencyCode.fromCode(code);
        if (currencyCode == null) {
            return ResponseEntity.badRequest().build();
        }
        currency.setCurrencyCode(currencyCode);
        Currency updatedCurrency = currencyService.createOrUpdateCurrency(currency);
        return ResponseEntity.ok(updatedCurrency);
    }
    
    @PutMapping("/{code}/rate")
    public ResponseEntity<Currency> updateRate(
            @PathVariable String code,
            @RequestBody BigDecimal newRate) {
        try {
            CurrencyCode currencyCode = CurrencyCode.fromCode(code);
            if (currencyCode == null) {
                return ResponseEntity.badRequest().build();
            }
            Currency updatedCurrency = currencyService.updateRate(currencyCode, newRate);
            return ResponseEntity.ok(updatedCurrency);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteCurrency(@PathVariable String code) {
        CurrencyCode currencyCode = CurrencyCode.fromCode(code);
        if (currencyCode == null) {
            return ResponseEntity.badRequest().build();
        }
        currencyService.deleteCurrency(currencyCode);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/convert")
    public ResponseEntity<BigDecimal> convertCurrency(
            @RequestParam BigDecimal amount,
            @RequestParam String sourceCurrency,
            @RequestParam String targetCurrency) {
        try {
            CurrencyCode source = CurrencyCode.fromCode(sourceCurrency);
            CurrencyCode target = CurrencyCode.fromCode(targetCurrency);
            if (source == null || target == null) {
                return ResponseEntity.badRequest().build();
            }
            BigDecimal convertedAmount = currencyService.convertCurrency(amount, source, target);
            return ResponseEntity.ok(convertedAmount);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 手動觸發匯率更新
     * 從 ExchangeRate-API 取得最新匯率並更新資料庫和 Redis
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshRates() {
        logger.info("收到手動觸發匯率更新請求");
        try {
            // 在背景執行緒中執行，避免阻塞 HTTP 請求
            new Thread(() -> {
                try {
                    currencyRateUpdateScheduler.updateExchangeRates();
                } catch (Exception e) {
                    logger.error("手動觸發匯率更新時發生錯誤", e);
                }
            }).start();
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "匯率更新任務已啟動，正在從 ExchangeRate-API 取得最新匯率");
            response.put("status", "processing");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("啟動匯率更新任務失敗", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "啟動匯率更新任務失敗: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 取得自動更新狀態
     */
    @GetMapping("/auto-update/status")
    public ResponseEntity<Map<String, Object>> getAutoUpdateStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("enabled", currencyRateUpdateScheduler.isAutoUpdateEnabled());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 啟用自動更新
     * 會立即執行一次更新，然後啟動每小時的排程任務
     */
    @PostMapping("/auto-update/enable")
    public ResponseEntity<Map<String, String>> enableAutoUpdate() {
        logger.info("收到啟用自動更新請求");
        try {
            // 在背景執行緒中執行，避免阻塞 HTTP 請求
            new Thread(() -> {
                try {
                    currencyRateUpdateScheduler.enableAutoUpdate();
                } catch (Exception e) {
                    logger.error("啟用自動更新時發生錯誤", e);
                }
            }).start();
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "自動更新已啟用，正在執行一次更新，之後將每小時自動更新一次");
            response.put("status", "enabled");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("啟動自動更新任務失敗", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "啟動自動更新任務失敗: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 停用自動更新
     */
    @PostMapping("/auto-update/disable")
    public ResponseEntity<Map<String, String>> disableAutoUpdate() {
        logger.info("收到停用自動更新請求");
        try {
            currencyRateUpdateScheduler.disableAutoUpdate();
            Map<String, String> response = new HashMap<>();
            response.put("message", "自動更新已停用");
            response.put("status", "disabled");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("停用自動更新失敗", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "停用自動更新失敗: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

