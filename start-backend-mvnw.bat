@echo off
chcp 65001 >nul
echo ========================================
echo 使用 Maven Wrapper 啟動後端
echo ========================================
echo.

echo 使用專案內的 Maven Wrapper，不需要全局安裝 Maven
echo.

mvnw.cmd spring-boot:run

pause


