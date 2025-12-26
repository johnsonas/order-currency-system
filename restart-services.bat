@echo off
chcp 65001 >nul
echo ========================================
echo 重新啟動服務
echo ========================================
echo.

echo [步驟 1] 停止現有服務...
taskkill /F /IM java.exe 2>nul
taskkill /F /FI "WINDOWTITLE eq Vue*Frontend*" 2>nul
timeout /t 2 /nobreak >nul

echo.
echo [步驟 2] 檢查資料庫...
docker ps --filter "name=order-currency-oracle" --format "{{.Names}}: {{.Status}}"

echo.
echo [步驟 3] 啟動後端 Spring Boot...
start "Spring Boot Backend" cmd /k "cd /d %~dp0 && mvn spring-boot:run"

timeout /t 10 /nobreak >nul

echo.
echo [步驟 4] 啟動前端 Vue 3...
cd frontend
start "Vue 3 Frontend" cmd /k "npm run dev"

cd ..
echo.
echo ========================================
echo 啟動完成！
echo ========================================
echo.
echo 請等待服務啟動完成（約 30-60 秒）
echo 後端: http://localhost:8080
echo 前端: http://localhost:5173
echo.
echo 請查看啟動視窗確認是否有錯誤訊息
echo.
pause


