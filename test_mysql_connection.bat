@echo off
setlocal EnableExtensions EnableDelayedExpansion
chcp 65001 > nul

if not exist out mkdir out

echo Dang bien dich file test ket noi MySQL...
powershell -NoProfile -Command "$files = Get-ChildItem -Recurse src -Filter *.java | %% { $_.FullName }; javac -encoding UTF-8 -cp 'out;lib/*' -d out $files"

if errorlevel 1 (
    echo.
    echo Bien dich that bai. Hay kiem tra JDK va thu vien mysql-connector-j trong lib.
    pause
    exit /b 1
)

echo.
echo Dang test ket noi MySQL...
java -cp "out;lib/*" auroracafe.store.MySqlConnectionTest

pause
