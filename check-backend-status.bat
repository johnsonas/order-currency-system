@echo off
chcp 65001 >nul
echo ========================================
echo 檢查後端狀態
echo ========================================
echo.

echo [1] 檢查 8080 端口...
netstat -ano | findstr ":8080"
if %errorlevel% equ 0 (
    echo 後端已啟動！端口 8080 正在監聽
) else (
    echo 後端尚未啟動（端口 8080 未監聽）
)

echo.
echo [2] 檢查 Java 進程...
tasklist | findstr "java.exe"
if %errorlevel% equ 0 (
    echo Java 進程正在運行
) else (
    echo 沒有 Java 進程運行
)

echo.
echo [3] 測試後端 API...
curl -s http://localhost:8080/api/orders >nul 2>&1
if %errorlevel% equ 0 (
    echo 後端 API 可以訪問！
) else (
    echo 後端 API 無法訪問
)

echo.
pause


