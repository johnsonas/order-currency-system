@echo off
chcp 65001 >nul
echo ========================================
echo 啟動 Spring Boot 應用程式
echo ========================================
echo.
echo 這會自動建立資料表（ORDERS 和 CURRENCIES）
echo.
pause

mvnw.cmd spring-boot:run

