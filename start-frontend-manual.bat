@echo off
chcp 65001 >nul
echo ========================================
echo 啟動前端 Vue 3
echo ========================================
echo.

cd frontend

echo 檢查 Node.js...
where node >nul 2>&1
if %errorlevel% neq 0 (
    echo [錯誤] 找不到 Node.js！
    echo 請先安裝 Node.js: https://nodejs.org/
    pause
    exit /b 1
)

echo Node.js 已安裝
echo.
echo 啟動開發伺服器...
npm run dev

pause


