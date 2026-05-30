@echo off
chcp 65001 >nul
echo Dang khoi dong Cafe Server tai cong 9000...
if not exist out mkdir out
javac -encoding UTF-8 -cp "lib/*;src" -d out @sources.txt
java -cp "out;lib/*" auroracafe.server.CafeServerApp
pause
