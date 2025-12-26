@echo off
chcp 65001 >nul 2>&1
echo ========================================
echo Starting Oracle Database Container
echo ========================================
echo.

echo Starting Oracle Database Free...
docker-compose -f docker-compose-oracle-free.yml up -d

echo.
echo Waiting for database to start (about 1-2 minutes)...
echo Please wait...

timeout /t 5 /nobreak >nul

echo.
echo Checking container status:
docker ps | findstr order-currency-oracle

echo.
echo Viewing startup logs (Press Ctrl+C to exit):
docker logs -f order-currency-oracle

pause
