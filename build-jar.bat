@echo off
chcp 65001 >nul
if not exist out mkdir out
if not exist dist mkdir dist
echo Dang bien dich source...
javac -encoding UTF-8 -cp "lib/*;src" -d out @sources.txt
if errorlevel 1 (
    echo Bien dich that bai.
    pause
    exit /b 1
)
echo Main-Class: auroracafe.AuroraCafeApp> manifest-client.txt
echo Main-Class: auroracafe.server.CafeServerApp> manifest-server.txt
jar cfm dist/aurora-cafe-client.jar manifest-client.txt -C out .
jar cfm dist/aurora-cafe-server.jar manifest-server.txt -C out .
echo Da tao file JAR trong thu muc dist.
pause
