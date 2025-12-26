@echo off
chcp 65001 >nul
echo ========================================
echo Maven 安裝指南
echo ========================================
echo.

echo [步驟 1] 檢查 Java 是否安裝...
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo [錯誤] 找不到 Java！
    echo 請先安裝 Java 17 或更高版本
    echo 下載：https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

java -version
echo.
echo [步驟 2] 檢查是否已安裝 Maven...
where mvn >nul 2>&1
if %errorlevel% equ 0 (
    echo Maven 已安裝！
    mvn -version
    pause
    exit /b 0
)

echo.
echo ========================================
echo Maven 未安裝，請手動安裝：
echo ========================================
echo.
echo 方法 1：使用 Chocolatey（推薦，最簡單）
echo   1. 以管理員身份開啟 PowerShell
echo   2. 執行：choco install maven
echo.
echo 方法 2：手動安裝
echo   1. 下載：https://maven.apache.org/download.cgi
echo     選擇：apache-maven-X.X.X-bin.zip
echo   2. 解壓縮到：C:\Program Files\Apache\maven
echo   3. 設定環境變數：
echo      - MAVEN_HOME = C:\Program Files\Apache\maven
echo      - PATH 新增：%%MAVEN_HOME%%\bin
echo   4. 重新開啟命令提示字元
echo.
echo 方法 3：使用 Scoop（如果已安裝）
echo   scoop install maven
echo.
pause


