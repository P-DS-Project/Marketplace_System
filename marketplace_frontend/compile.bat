@echo off
echo ==========================================
echo   MarketPlace Pro - JavaFX Frontend
echo   Compiling...
echo ==========================================

if not exist out mkdir out

set JAVAFX_PATH=lib
set LIB_PATH=lib\*

echo Finding Java source files...
powershell -Command "$files = Get-ChildItem -Path 'src' -Recurse -Filter '*.java' | Select-Object -ExpandProperty FullName; javac -d out --module-path %JAVAFX_PATH% --add-modules javafx.controls -cp '%LIB_PATH%' -sourcepath src $files"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ==========================================
    echo   Compilation SUCCESSFUL!
    echo   Classes saved in 'out' folder.
    echo ==========================================
    
    echo Copying resources...
    if not exist out\styles mkdir out\styles
    xcopy /Y /Q resources\styles\*.css out\styles\ >nul 2>&1
    echo Resources copied.
) else (
    echo.
    echo ==========================================
    echo   Compilation FAILED! Check errors above.
    echo ==========================================
)
