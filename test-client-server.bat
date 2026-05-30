@echo off
chcp 65001 >nul
echo Test ket noi Client -> Server...
if not exist out mkdir out
javac -encoding UTF-8 -cp "lib/*;src" -d out @sources.txt
java -cp "out;lib/*" auroracafe.client.CafeClientPing localhost 9000
pause
