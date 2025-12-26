@echo off
chcp 65001 >nul
echo ========================================
echo 修復匯率資料
echo ========================================
echo.

echo 執行 SQL 更新匯率...
docker exec -i order-currency-oracle sqlplus ordersystem/ordersystem123@FREEPDB1 < fix-currency-rates.sql

echo.
echo ========================================
echo 匯率已更新！
echo ========================================
echo.
echo 現在請重新測試換算功能
echo 例如：1222 TWD 應該換算為約 39.10 USD
echo.
pause


