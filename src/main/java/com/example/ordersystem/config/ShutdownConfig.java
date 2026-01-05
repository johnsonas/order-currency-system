package com.example.ordersystem.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 應用關閉配置
 * 確保在應用關閉時正確清理資源，避免內存洩漏
 */
@Configuration
public class ShutdownConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(ShutdownConfig.class);
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @PreDestroy
    public void destroy() {
        logger.info("開始關閉應用程式，清理資源...");
        
        try {
            // 關閉 HikariCP 連接池
            if (applicationContext != null) {
                try {
                    DataSource dataSource = applicationContext.getBean(DataSource.class);
                    if (dataSource instanceof HikariDataSource) {
                        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                        logger.info("正在關閉 HikariCP 連接池...");
                        hikariDataSource.close();
                        logger.info("HikariCP 連接池已關閉");
                    }
                } catch (Exception e) {
                    logger.warn("關閉連接池時發生錯誤（可忽略）: {}", e.getMessage());
                }
            }
            
            // 等待一小段時間，讓 Oracle JDBC 驅動的線程有時間清理
            Thread.sleep(500);
            
            logger.info("資源清理完成");
        } catch (Exception e) {
            logger.error("關閉應用程式時發生錯誤", e);
        }
    }
}

