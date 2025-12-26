@echo off
chcp 65001 >nul 2>&1
echo ========================================
echo Starting Spring Boot Application
echo ========================================
echo.
echo This will automatically create tables (ORDERS and CURRENCIES)
echo.

REM Check if JAVA_HOME is set
setlocal enabledelayedexpansion
set "JAVA_HOME_ORIG=%JAVA_HOME%"
set "JAVA_HOME="
set "FOUND_JAVA=0"

REM If JAVA_HOME is set, trim whitespace and validate
if not "%JAVA_HOME_ORIG%"=="" (
    REM Trim leading and trailing spaces
    for /f "tokens=* delims= " %%a in ("%JAVA_HOME_ORIG%") do set "JAVA_HOME=%%a"
    REM Check if path exists and has java.exe
    if exist "!JAVA_HOME!\bin\java.exe" (
        set "FOUND_JAVA=1"
        echo JAVA_HOME is set to: !JAVA_HOME!
    ) else (
        echo [WARNING] JAVA_HOME is set but invalid: "%JAVA_HOME_ORIG%"
        echo Trying to find Java automatically...
        set "JAVA_HOME="
    )
)

REM If JAVA_HOME not set or invalid, try to find Java automatically
if "!FOUND_JAVA!"=="0" (
    REM Try to find Java using where command
    where java >nul 2>&1
    if !errorlevel! equ 0 (
        for /f "delims=" %%i in ('where java') do (
            set "java_path=%%i"
            goto found_java_path
        )
        :found_java_path
        if defined java_path (
            REM Extract JAVA_HOME from java.exe path (remove \bin\java.exe)
            for %%i in ("!java_path!") do (
                set "java_dir=%%~dpi"
                set "java_dir=!java_dir:~0,-1!"
                for %%j in ("!java_dir!") do (
                    set "JAVA_HOME=%%~dpj"
                    set "JAVA_HOME=!JAVA_HOME:~0,-1!"
                    REM Trim spaces
                    for /f "tokens=* delims= " %%a in ("!JAVA_HOME!") do set "JAVA_HOME=%%a"
                    if exist "!JAVA_HOME!\bin\java.exe" (
                        set "FOUND_JAVA=1"
                    )
                )
            )
        )
    )
    
    REM If not found via where, try common locations
    if "!FOUND_JAVA!"=="0" (
        REM Check Eclipse Adoptium locations
        for /d %%d in ("C:\Program Files\Eclipse Adoptium\jdk-*") do (
            if exist "%%d\bin\java.exe" (
                set "JAVA_HOME=%%d"
                set "FOUND_JAVA=1"
                goto found_java_location
            )
        )
        :found_java_location
        
        REM Check standard Java locations
        if "!FOUND_JAVA!"=="0" (
            if exist "C:\Program Files\Java\jdk-17" (
                set "JAVA_HOME=C:\Program Files\Java\jdk-17"
                set "FOUND_JAVA=1"
            ) else if exist "C:\Program Files\Java\jdk-21" (
                set "JAVA_HOME=C:\Program Files\Java\jdk-21"
                set "FOUND_JAVA=1"
            ) else if exist "C:\Program Files\Java\jdk-11" (
                set "JAVA_HOME=C:\Program Files\Java\jdk-11"
                set "FOUND_JAVA=1"
            ) else if exist "C:\Program Files (x86)\Java\jdk-17" (
                set "JAVA_HOME=C:\Program Files (x86)\Java\jdk-17"
                set "FOUND_JAVA=1"
            ) else if exist "C:\Program Files (x86)\Java\jdk-21" (
                set "JAVA_HOME=C:\Program Files (x86)\Java\jdk-21"
                set "FOUND_JAVA=1"
            )
        )
    )
    
    if "!FOUND_JAVA!"=="1" (
        echo Found Java at: !JAVA_HOME!
    ) else (
        echo.
        echo [ERROR] Java not found automatically!
        echo.
        echo Please set JAVA_HOME environment variable before running.
        echo Example: set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot
        echo.
        endlocal
        pause
        exit /b 1
    )
)

REM Verify JAVA_HOME path exists before proceeding
if not exist "!JAVA_HOME!\bin\java.exe" (
    echo [ERROR] JAVA_HOME path is invalid: !JAVA_HOME!
    echo Please check your Java installation.
    endlocal
    pause
    exit /b 1
)

endlocal & set "JAVA_HOME=%JAVA_HOME%"

echo.
pause

if not "%JAVA_HOME%"=="" (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

mvnw.cmd spring-boot:run
