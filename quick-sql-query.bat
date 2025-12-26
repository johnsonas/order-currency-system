@echo off
chcp 65001 >nul
echo ========================================
echo Oracle 資料庫快速查詢工具
echo ========================================
echo.
echo 這個腳本會開啟 SQL*Plus，你可以直接執行 SQL 查詢
echo.
echo 常用指令：
echo   - SELECT * FROM USER_TABLES;     (查看所有資料表)
echo   - SELECT * FROM CURRENCIES;      (查看幣別資料)
echo   - SELECT * FROM ORDERS;          (查看訂單資料)
echo   - EXIT;                          (退出)
echo.
pause

docker exec -it order-currency-oracle sqlplus sys/Oracle123@freepdb1 as sysdba


