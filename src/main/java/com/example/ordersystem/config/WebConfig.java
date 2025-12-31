package com.example.ordersystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置類
 * 用於配置全局的 CORS（跨域資源共享）設定
 * 
 * 最佳實踐：
 * - 將 CORS 配置集中在一個地方，而不是分散在各個 Controller
 * - 從 application.properties 讀取配置值，方便不同環境使用不同設定
 * - 符合配置外部化的最佳實踐
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${cors.allowed.origins}")
    private String allowedOrigins;
    
    @Value("${cors.allowed.methods}")
    private String allowedMethods;
    
    @Value("${cors.allowed.headers}")
    private String allowedHeaders;
    
    @Value("${cors.allow.credentials}")
    private boolean allowCredentials;
    
    @Value("${cors.max.age}")
    private long maxAge;
    
    /**
     * 配置全局 CORS 設定
     * 
     * @param registry CORS 註冊器
     */
    @Override
    @SuppressWarnings("null")
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        // 將逗號分隔的字串轉換為陣列，並去除空白
        String[] origins = allowedOrigins.split(",\\s*");
        String[] methods = allowedMethods.split(",\\s*");
        String[] headers = "*".equals(allowedHeaders.trim()) 
                ? new String[]{"*"} 
                : allowedHeaders.split(",\\s*");
        
        registry.addMapping("/api/**")  // 允許所有 /api 開頭的端點
                .allowedOrigins(origins)  // 從配置讀取允許的前端來源
                .allowedMethods(methods)  // 從配置讀取允許的 HTTP 方法
                .allowedHeaders(headers)  // 從配置讀取允許的請求標頭
                .allowCredentials(allowCredentials)  // 從配置讀取是否允許認證資訊
                .maxAge(maxAge);  // 從配置讀取預檢請求的緩存時間
    }
}

