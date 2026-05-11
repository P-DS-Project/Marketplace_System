@echo off
echo ==========================================
echo   MarketPlace Pro - Launching...
echo ==========================================

set JAVAFX_PATH=lib

java "-Djava.library.path=lib" --module-path %JAVAFX_PATH% --add-modules javafx.controls --enable-native-access=javafx.graphics -cp "out;lib\*" Main
