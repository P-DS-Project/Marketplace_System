@echo off
if "%~1"=="" (
    echo Please specify the class you want to run.
    echo Usage:   run.bat [ClassName]
    echo Example: run.bat App
    echo Example: run.bat MarketplaceServer
    exit /b
)
echo Running %~1...
java -cp "out;lib/*" %~1
