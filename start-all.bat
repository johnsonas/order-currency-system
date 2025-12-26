@echo off
chcp 65001 >nul
echo ========================================
echo 啟動前後端服務
echo ========================================
echo.
echo 這會啟動：
echo 1. 後端 Spring Boot (http://localhost:8080)
echo 2. 前端 Vue 3 (http://localhost:5173)
echo.
echo 請保持視窗開啟，按 Ctrl+C 可停止服務
echo.
pause

echo.
echo [1/2] 啟動後端 Spring Boot...
start "Spring Boot Backend" cmd /k "mvnw.cmd spring-boot:run"

timeout /t 5 /nobreak >nul

echo.
echo [2/2] 啟動前端 Vue 3...
cd frontend
start "Vue 3 Frontend" cmd /k "npm run dev"

cd ..
echo.
echo ========================================
echo 啟動完成！
echo ========================================
echo.
echo 後端: http://localhost:8080
echo 前端: http://localhost:5173
echo.
echo 請等待服務啟動完成...
echo.
pause

