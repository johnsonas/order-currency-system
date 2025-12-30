@echo off
chcp 65001 >nul 2>&1
echo ========================================
echo Running Tests
echo ========================================
echo.

echo [1/3] Running all tests...
call mvn clean test
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Tests failed!
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [2/3] Generating test coverage report...
call mvn jacoco:report
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo WARNING: Coverage report generation failed!
)

echo.
echo [3/3] Opening coverage report...
if exist "target\site\jacoco\index.html" (
    start "" "target\site\jacoco\index.html"
    echo Coverage report opened in browser.
) else (
    echo Coverage report not found.
)

echo.
echo ========================================
echo Test execution completed!
echo ========================================
pause



