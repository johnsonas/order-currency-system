package com.example.ordersystem.service;

import com.example.ordersystem.dto.ExchangeRateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * ExchangeRate-API 服務
 * 用於從 ExchangeRate-API 取得最新匯率
 */
@Service
public class ExchangeRateApiService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateApiService.class);
    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/USD";
    
    @Autowired
    private RestTemplate restTemplate;
    
    /**
     * 從 ExchangeRate-API 取得最新匯率
     * 
     * @return 匯率 Map，key 為幣別代碼，value 為對 USD 的匯率
     */
    public Map<String, BigDecimal> fetchLatestRates() {
        logger.info("=== 開始呼叫外部 API 取得匯率 ===");
        logger.info("API URL: {}", API_URL);
        long startTime = System.currentTimeMillis();
        
        try {
            logger.info("正在呼叫 ExchangeRate-API...");
            ResponseEntity<ExchangeRateResponse> response = restTemplate.getForEntity(
                API_URL, 
                ExchangeRateResponse.class
            );
            
            long apiCallTime = System.currentTimeMillis() - startTime;
            logger.info("API 呼叫完成，耗時: {} ms，HTTP 狀態碼: {}", apiCallTime, response.getStatusCode());
            
            ExchangeRateResponse apiResponse = response.getBody();
            if (response.getStatusCode().is2xxSuccessful() && apiResponse != null) {
                logger.info("API 回應解析成功，base: {}, date: {}", apiResponse.getBase(), apiResponse.getDate());
                
                Map<String, Double> rates = apiResponse.getRates();
                
                if (rates == null || rates.isEmpty()) {
                    logger.warn("API 回應中的匯率資料為空");
                    return new HashMap<>();
                }
                
                logger.info("API 返回了 {} 種幣別的匯率資料", rates.size());
                
                // 轉換為 BigDecimal 並計算對 TWD 的匯率
                Map<String, BigDecimal> result = new HashMap<>();
                
                // 取得 TWD 對 USD 的匯率
                Double twdToUsd = rates.get("TWD");
                if (twdToUsd == null) {
                    logger.warn("API 回應中找不到 TWD 匯率");
                    return result;
                }
                
                logger.info("TWD 對 USD 匯率: {}", twdToUsd);
                
                // 計算各幣別對 TWD 的匯率
                // 如果 API 返回的是對 USD 的匯率，需要轉換為對 TWD 的匯率
                // rate_to_twd = 1 / (rate_to_usd / twd_to_usd) = twd_to_usd / rate_to_usd
                
                for (Map.Entry<String, Double> entry : rates.entrySet()) {
                    String currencyCode = entry.getKey();
                    Double rateToUsd = entry.getValue();
                    
                    // 計算對 TWD 的匯率
                    BigDecimal rateToTwd = BigDecimal.valueOf(twdToUsd)
                        .divide(BigDecimal.valueOf(rateToUsd), 6, java.math.RoundingMode.HALF_UP);
                    
                    result.put(currencyCode, rateToTwd);
                }
                
                long totalTime = System.currentTimeMillis() - startTime;
                logger.info("=== 外部 API 呼叫完成 ===");
                logger.info("成功取得 {} 種幣別的匯率，總耗時: {} ms", result.size(), totalTime);
                logger.debug("匯率資料: {}", result);
                return result;
            } else {
                logger.error("API 呼叫失敗，HTTP 狀態碼: {}", response.getStatusCode());
                return new HashMap<>();
            }
        } catch (RestClientException e) {
            long totalTime = System.currentTimeMillis() - startTime;
            logger.error("=== 外部 API 呼叫發生錯誤 ===");
            logger.error("錯誤訊息: {}", e.getMessage());
            logger.error("總耗時: {} ms", totalTime, e);
            return new HashMap<>();
        }
    }
}

