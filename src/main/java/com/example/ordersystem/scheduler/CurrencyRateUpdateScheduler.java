package com.example.ordersystem.scheduler;

import com.example.ordersystem.model.Currency;
import com.example.ordersystem.model.CurrencyCode;
import com.example.ordersystem.service.CurrencyService;
import com.example.ordersystem.service.ExchangeRateApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * 匯率更新排程任務
 * 每小時自動從 ExchangeRate-API 取得最新匯率並更新資料庫和 Redis
 */
@Component
public class CurrencyRateUpdateScheduler implements ApplicationListener<ApplicationReadyEvent> {
    
    private static final Logger logger = LoggerFactory.getLogger(CurrencyRateUpdateScheduler.class);
    private static final String CRON_EXPRESSION = "0 0 * * * ?"; // 每小時的 0 分 0 秒執行
    
    @Autowired
    private ExchangeRateApiService exchangeRateApiService;
    
    @Autowired
    private CurrencyService currencyService;
    
    @Autowired
    private TaskScheduler taskScheduler;
    
    private ScheduledFuture<?> scheduledTask;
    private volatile boolean autoUpdateEnabled = true; // 預設啟用自動更新
    
    /**
     * 更新匯率的核心方法
     * 會自動更新資料庫和 Redis 快取
     * 
     * 重要：執行完後會自動重新安排下一次執行（如果自動更新已啟用）
     */
    public void updateExchangeRates() {
        logger.info("========================================");
        logger.info("開始執行排程任務：匯率自動更新");
        logger.info("執行時間: {}", java.time.LocalDateTime.now());
        logger.info("========================================");
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 從 API 取得最新匯率
            logger.info("步驟 1/3: 呼叫外部 API 取得最新匯率");
            Map<String, BigDecimal> latestRates = exchangeRateApiService.fetchLatestRates();
            
            if (latestRates.isEmpty()) {
                logger.warn("未取得任何匯率資料，跳過更新");
                // 即使失敗，如果自動更新啟用，也要重新安排下一次執行
                rescheduleIfEnabled();
                return;
            }
            
            logger.info("步驟 2/3: 開始更新資料庫和 Redis");
            final int[] updatedCount = {0};
            final int[] createdCount = {0};
            
            // 更新系統支援的幣別
            for (CurrencyCode currencyCode : CurrencyCode.values()) {
                String code = currencyCode.getCode();
                BigDecimal newRate = latestRates.get(code);
                
                if (newRate != null && newRate.compareTo(BigDecimal.ZERO) > 0) {
                    try {
                        logger.info("處理幣別: {}, 新匯率: {}", code, newRate);
                        
                        // 檢查幣別是否存在
                        currencyService.getCurrencyByCode(currencyCode).ifPresentOrElse(
                            // 如果存在，更新匯率
                            existingCurrency -> {
                                logger.info("幣別 {} 已存在，更新匯率: {} -> {}", code, existingCurrency.getRateToTwd(), newRate);
                                currencyService.updateRate(currencyCode, newRate);
                                updatedCount[0]++;
                            },
                            // 如果不存在，建立新的幣別
                            () -> {
                                logger.info("幣別 {} 不存在，建立新幣別，匯率: {}", code, newRate);
                                Currency newCurrency = new Currency();
                                newCurrency.setCurrencyCode(currencyCode);
                                newCurrency.setRateToTwd(newRate);
                                currencyService.createOrUpdateCurrency(newCurrency);
                                createdCount[0]++;
                            }
                        );
                    } catch (Exception e) {
                        logger.error("更新幣別 {} 時發生錯誤: {}", code, e.getMessage(), e);
                    }
                } else {
                    logger.warn("幣別 {} 的匯率無效或不存在於 API 回應中", code);
                }
            }
            
            long totalTime = System.currentTimeMillis() - startTime;
            logger.info("步驟 3/3: 更新完成");
            logger.info("========================================");
            logger.info("排程任務執行完成");
            logger.info("更新幣別數: {}", updatedCount[0]);
            logger.info("新增幣別數: {}", createdCount[0]);
            logger.info("總耗時: {} ms", totalTime);
            logger.info("========================================");
            
            // 執行完後，如果自動更新啟用，重新安排下一次執行
            rescheduleIfEnabled();
            
        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;
            logger.error("========================================");
            logger.error("排程任務執行失敗");
            logger.error("錯誤訊息: {}", e.getMessage());
            logger.error("總耗時: {} ms", totalTime);
            logger.error("========================================", e);
            
            // 即使失敗，如果自動更新啟用，也要重新安排下一次執行
            rescheduleIfEnabled();
        }
    }
    
    /**
     * 如果自動更新已啟用，重新安排下一次執行
     * 
     * 原理：
     * 1. TaskScheduler.schedule() 使用 CronTrigger 時，只會執行一次
     * 2. 所以需要在每次執行完後，重新調用 schedule() 來安排下一次執行
     * 3. CronTrigger 會自動計算下一次執行時間（例如：下一個整點）
     * 4. 線程會進入等待狀態，直到下一次執行時間到達，不消耗 CPU
     * 
     * 執行流程：
     * 14:00:00 - 執行 updateExchangeRates()
     * 14:00:05 - 執行完畢，調用 rescheduleIfEnabled()
     * 14:00:05 - CronTrigger 計算：下一次是 15:00:00
     * 14:00:05 - 線程進入等待狀態（不消耗 CPU）
     * 15:00:00 - 時間到達，執行 updateExchangeRates()
     * 15:00:05 - 執行完畢，再次調用 rescheduleIfEnabled()
     * ...（無限循環）
     */
    private synchronized void rescheduleIfEnabled() {
        if (!autoUpdateEnabled) {
            logger.debug("自動更新已停用，不重新安排下一次執行");
            return;
        }
        
        try {
            // 清理舊的任務引用
            scheduledTask = null;
            
            // 重新安排下一次執行
            // CronTrigger 會自動根據當前時間計算下一次執行時間
            CronTrigger trigger = new CronTrigger(CRON_EXPRESSION);
            scheduledTask = taskScheduler.schedule(this::updateExchangeRates, trigger);
            
            if (scheduledTask != null) {
                logger.info("下一次執行已安排，將在每小時整點自動執行");
                logger.debug("線程將進入等待狀態，直到下一次執行時間到達（不消耗 CPU）");
            } else {
                logger.error("重新安排下一次執行失敗，scheduledTask 為 null");
            }
        } catch (Exception e) {
            logger.error("重新安排下一次執行時發生錯誤", e);
        }
    }
    
    /**
     * 應用程式啟動完成後立即執行一次匯率更新
     * 確保應用程式完全啟動（包括資料庫連接、Redis 連接等）後才執行
     */
    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        logger.info("========================================");
        logger.info("應用程式啟動完成，開始執行初始匯率更新");
        logger.info("啟動時間: {}", java.time.LocalDateTime.now());
        logger.info("========================================");
        updateExchangeRates();
        logger.info("初始匯率更新完成，自動更新狀態: {}", autoUpdateEnabled ? "已啟用（每小時更新一次）" : "已停用");
        
        // 如果啟用自動更新，啟動排程任務
        if (autoUpdateEnabled) {
            startScheduledTask();
        }
    }
    
    /**
     * 啟動排程任務（每小時更新一次）
     * 
     * 原理說明：
     * 1. TaskScheduler.schedule() 使用 CronTrigger 時，只會執行一次
     * 2. 所以在 updateExchangeRates() 執行完後，需要重新調用 schedule() 來安排下一次執行
     * 3. CronTrigger 會自動計算下一次執行時間（例如：下一個整點）
     * 4. 線程會進入等待狀態，直到下一次執行時間到達，不消耗 CPU
     */
    private synchronized void startScheduledTask() {
        // 檢查是否已經有運行中的任務
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            logger.warn("排程任務已在運行中，跳過重複啟動");
            return;
        }
        
        // 如果任務已取消但 reference 還在，先清理
        if (scheduledTask != null) {
            scheduledTask = null;
        }
        
        logger.info("啟動自動更新排程任務（每小時更新一次）");
        logger.info("Cron 表達式: {} (每小時的 0 分 0 秒執行)", CRON_EXPRESSION);
        
        try {
            // CronTrigger 會自動根據當前時間計算下一次執行時間
            CronTrigger trigger = new CronTrigger(CRON_EXPRESSION);
            scheduledTask = taskScheduler.schedule(this::updateExchangeRates, trigger);
            
            if (scheduledTask != null) {
                logger.info("排程任務啟動成功，將在每小時整點自動執行");
                logger.info("執行完後會自動重新安排下一次執行（每小時重複）");
                logger.debug("線程將進入等待狀態，直到下一次執行時間到達（不消耗 CPU）");
            } else {
                logger.error("排程任務啟動失敗，scheduledTask 為 null");
            }
        } catch (Exception e) {
            logger.error("啟動排程任務時發生錯誤", e);
            scheduledTask = null;
        }
    }
    
    /**
     * 停止排程任務
     */
    private synchronized void stopScheduledTask() {
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            logger.info("停止自動更新排程任務");
            boolean cancelled = scheduledTask.cancel(false);
            if (cancelled) {
                logger.info("排程任務已成功取消");
            } else {
                logger.warn("排程任務取消失敗（可能正在執行中）");
            }
            scheduledTask = null;
        } else {
            logger.info("沒有運行中的排程任務需要停止");
        }
    }
    
    /**
     * 啟用自動更新
     * 會立即執行一次更新，然後啟動每小時的排程任務
     */
    public synchronized void enableAutoUpdate() {
        if (autoUpdateEnabled) {
            logger.info("自動更新已經啟用");
            return;
        }
        
        logger.info("========================================");
        logger.info("啟用自動更新匯率功能");
        logger.info("========================================");
        
        autoUpdateEnabled = true;
        
        // 立即執行一次更新
        logger.info("立即執行一次匯率更新...");
        updateExchangeRates();
        
        // 啟動排程任務
        startScheduledTask();
        
        logger.info("自動更新已啟用，將每小時自動更新一次");
    }
    
    /**
     * 停用自動更新
     */
    public synchronized void disableAutoUpdate() {
        if (!autoUpdateEnabled) {
            logger.info("自動更新已經停用");
            return;
        }
        
        logger.info("========================================");
        logger.info("停用自動更新匯率功能");
        logger.info("========================================");
        
        autoUpdateEnabled = false;
        stopScheduledTask();
        
        logger.info("自動更新已停用");
    }
    
    /**
     * 取得自動更新狀態
     */
    public boolean isAutoUpdateEnabled() {
        return autoUpdateEnabled;
    }
}

