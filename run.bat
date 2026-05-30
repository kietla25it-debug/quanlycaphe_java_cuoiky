@echo off
setlocal EnableExtensions EnableDelayedExpansion
chcp 65001 > nul

if not exist out mkdir out

echo Dang bien dich chuong trinh...
powershell -NoProfile -Command "$files = Get-ChildItem -Recurse src -Filter *.java | %% { $_.FullName }; javac -encoding UTF-8 -cp 'out;lib/*' -d out $files"

if errorlevel 1 (
    echo.
    echo Bien dich that bai. Hay kiem tra JDK trong IntelliJ/May tinh.
    pause
    exit /b 1
)

echo.
echo Dang chay ung dung voi classpath: out;lib/*
java -cp "out;lib/*" auroracafe.AuroraCafeApp

pause
