@echo off
chcp 65001 >nul
echo ========================================
echo 啟動後端 Spring Boot
echo ========================================
echo.

cd /d %~dp0

echo 使用 Maven Wrapper 啟動...
mvnw.cmd spring-boot:run

pause


