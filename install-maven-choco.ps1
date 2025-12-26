# Maven 安裝腳本（使用 Chocolatey）
# 需要以管理員身份執行

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Maven 安裝腳本（Chocolatey）" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 檢查是否以管理員身份執行
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if (-not $isAdmin) {
    Write-Host "[錯誤] 請以管理員身份執行此腳本！" -ForegroundColor Red
    Write-Host "右鍵點擊 PowerShell → 以系統管理員身分執行" -ForegroundColor Yellow
    pause
    exit 1
}

# 檢查 Chocolatey 是否安裝
Write-Host "[步驟 1] 檢查 Chocolatey..." -ForegroundColor Yellow
$chocoInstalled = Get-Command choco -ErrorAction SilentlyContinue

if (-not $chocoInstalled) {
    Write-Host "[提示] Chocolatey 未安裝，正在安裝..." -ForegroundColor Yellow
    Set-ExecutionPolicy Bypass -Scope Process -Force
    [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
    iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[錯誤] Chocolatey 安裝失敗！" -ForegroundColor Red
        pause
        exit 1
    }
    
    # 重新載入環境變數
    $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
}

Write-Host "[步驟 2] 安裝 Maven..." -ForegroundColor Yellow
choco install maven -y

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "[成功] Maven 安裝完成！" -ForegroundColor Green
    Write-Host ""
    Write-Host "請重新開啟命令提示字元，然後執行：mvn -version" -ForegroundColor Yellow
} else {
    Write-Host ""
    Write-Host "[錯誤] Maven 安裝失敗！" -ForegroundColor Red
}

Write-Host ""
pause


