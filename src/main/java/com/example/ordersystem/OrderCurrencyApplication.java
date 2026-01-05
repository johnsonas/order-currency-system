package com.example.ordersystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

@SpringBootApplication
@EnableScheduling
public class OrderCurrencyApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderCurrencyApplication.class);

    public static void main(String[] args) {
        // 註冊關閉鉤子，確保應用關閉時清理資源
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("應用程式正在關閉，清理 JDBC 驅動...");
            try {
                // 註銷所有 JDBC 驅動
                Enumeration<Driver> drivers = DriverManager.getDrivers();
                while (drivers.hasMoreElements()) {
                    Driver driver = drivers.nextElement();
                    try {
                        DriverManager.deregisterDriver(driver);
                        logger.debug("已註銷 JDBC 驅動: {}", driver.getClass().getName());
                    } catch (SQLException e) {
                        logger.warn("註銷 JDBC 驅動時發生錯誤: {}", driver.getClass().getName(), e);
                    }
                }
            } catch (Exception e) {
                logger.error("清理 JDBC 驅動時發生錯誤", e);
            }
        }));
        
        SpringApplication.run(OrderCurrencyApplication.class, args);
    }
}

