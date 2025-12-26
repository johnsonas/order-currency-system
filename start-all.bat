@echo off
chcp 65001 >nul 2>&1
echo ========================================
echo Starting Backend and Frontend Services
echo ========================================
echo.
echo This will start:
echo 1. Frontend Vue 3 (http://localhost:5173)
echo 2. Backend Spring Boot (http://localhost:8080)
echo.
echo Keep this window open, press Ctrl+C to stop services
echo.
pause

echo.
echo [1/2] Starting Vue 3 Frontend...
start "Vue 3 Frontend" cmd /k "cd /d %~dp0frontend && npm run dev"

echo.
echo Waiting for frontend to start...
echo Checking if frontend is ready on port 5173...

set /a check_count=0
set /a max_checks=20

:check_frontend
set /a check_count+=1
timeout /t 2 /nobreak >nul
netstat -an | findstr ":5173" >nul
if %errorlevel% equ 0 (
    echo.
    echo [OK] Frontend is ready! Port 5173 is listening.
    goto frontend_ready
) else (
    if %check_count% geq %max_checks% (
        echo.
        echo [WARNING] Frontend check timeout after 40 seconds.
        echo Please check the "Vue 3 Frontend" window manually.
        echo Continuing to start backend...
        goto frontend_ready
    ) else (
        echo Still waiting... (%check_count%/%max_checks%)
        goto check_frontend
    )
)

:frontend_ready
echo.
echo [2/2] Starting Spring Boot Backend...

REM Check and fix JAVA_HOME
setlocal enabledelayedexpansion
set "FOUND_JAVA=0"
set "DETECTED_JAVA_HOME="

REM If JAVA_HOME is set, check and trim it
if not "%JAVA_HOME%"=="" (
    REM Trim spaces by using a temp variable
    set "TEMP_JAVA_HOME=%JAVA_HOME%"
    REM Remove trailing spaces
    :trim_loop
    if "!TEMP_JAVA_HOME:~-1!"==" " set "TEMP_JAVA_HOME=!TEMP_JAVA_HOME:~0,-1!" & goto trim_loop
    REM Remove leading spaces
    :trim_lead
    if "!TEMP_JAVA_HOME:~0,1!"==" " set "TEMP_JAVA_HOME=!TEMP_JAVA_HOME:~1!" & goto trim_lead
    
    if exist "!TEMP_JAVA_HOME!\bin\java.exe" (
        set "DETECTED_JAVA_HOME=!TEMP_JAVA_HOME!"
        set "FOUND_JAVA=1"
        echo JAVA_HOME found: !DETECTED_JAVA_HOME!
    ) else (
        echo [WARNING] JAVA_HOME is set but invalid: "%JAVA_HOME%"
        echo Trying to find Java automatically...
    )
)

REM If not found, try to find Java automatically
if "!FOUND_JAVA!"=="0" (
    REM Check Eclipse Adoptium locations first
    if exist "C:\Program Files\Eclipse Adoptium" (
        for /d %%d in ("C:\Program Files\Eclipse Adoptium\jdk-*") do (
            if exist "%%d\bin\java.exe" (
                set "DETECTED_JAVA_HOME=%%d"
                set "FOUND_JAVA=1"
                goto found_java
            )
        )
    )
    
    REM Try to find Java using where command
    where java >nul 2>&1
    if !errorlevel! equ 0 (
        for /f "delims=" %%i in ('where java 2^>nul') do (
            set "java_path=%%i"
            goto found_java_path
        )
        :found_java_path
        if defined java_path (
            REM Extract JAVA_HOME from java.exe path
            for %%i in ("!java_path!") do (
                set "java_dir=%%~dpi"
                set "java_dir=!java_dir:~0,-1!"
                for %%j in ("!java_dir!") do (
                    set "DETECTED_JAVA_HOME=%%~dpj"
                    set "DETECTED_JAVA_HOME=!DETECTED_JAVA_HOME:~0,-1!"
                    REM Trim spaces
                    :trim_java_home
                    if "!DETECTED_JAVA_HOME:~-1!"==" " set "DETECTED_JAVA_HOME=!DETECTED_JAVA_HOME:~0,-1!" & goto trim_java_home
                    if exist "!DETECTED_JAVA_HOME!\bin\java.exe" (
                        set "FOUND_JAVA=1"
                        goto found_java
                    )
                )
            )
        )
    )
    
    REM Check standard Java locations
    if "!FOUND_JAVA!"=="0" (
        if exist "C:\Program Files\Java\jdk-17\bin\java.exe" (
            set "DETECTED_JAVA_HOME=C:\Program Files\Java\jdk-17"
            set "FOUND_JAVA=1"
        ) else if exist "C:\Program Files\Java\jdk-21\bin\java.exe" (
            set "DETECTED_JAVA_HOME=C:\Program Files\Java\jdk-21"
            set "FOUND_JAVA=1"
        ) else if exist "C:\Program Files\Java\jdk-11\bin\java.exe" (
            set "DETECTED_JAVA_HOME=C:\Program Files\Java\jdk-11"
            set "FOUND_JAVA=1"
        )
    )
    
    :found_java
    if "!FOUND_JAVA!"=="1" (
        echo Found Java at: !DETECTED_JAVA_HOME!
    ) else (
        echo.
        echo [ERROR] Java not found automatically!
        echo.
        echo Please set JAVA_HOME environment variable manually.
        echo Example: set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot
        echo.
        endlocal
        pause
        exit /b 1
    )
)

REM Verify JAVA_HOME path exists
if not exist "!DETECTED_JAVA_HOME!\bin\java.exe" (
    echo [ERROR] JAVA_HOME path is invalid: !DETECTED_JAVA_HOME!
    echo Please check your Java installation.
    endlocal
    pause
    exit /b 1
)

REM Create a temporary batch file to start backend with JAVA_HOME
set "TEMP_BATCH=%TEMP%\start-backend-%RANDOM%.bat"
(
    echo @echo off
    echo cd /d "%~dp0"
    echo set JAVA_HOME=!DETECTED_JAVA_HOME!
    echo if not exist "%%JAVA_HOME%%\bin\java.exe" ^(
    echo     echo [ERROR] JAVA_HOME path is invalid: %%JAVA_HOME%%
    echo     pause
    echo     exit /b 1
    echo ^)
    echo mvnw.cmd spring-boot:run
) > "%TEMP_BATCH%"

start "Spring Boot Backend" cmd /k "%TEMP_BATCH%"
endlocal
echo.
echo ========================================
echo Services Started!
echo ========================================
echo.
echo Backend: http://localhost:8080
echo Frontend: http://localhost:5173
echo.
echo Please wait for services to start...
echo.
pause
