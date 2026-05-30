@echo off
setlocal
if not exist lib mkdir lib
if exist lib\mysql-connector-j-9.6.0.jar (
  echo Da co san mysql-connector-j-9.6.0.jar
  exit /b 0
)
powershell -NoProfile -Command "Invoke-WebRequest -UseBasicParsing 'https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.6.0/mysql-connector-j-9.6.0.jar' -OutFile 'lib/mysql-connector-j-9.6.0.jar'"
if exist lib\mysql-connector-j-9.6.0.jar (
  echo Tai driver MySQL thanh cong.
) else (
  echo Khong tai duoc driver MySQL.
)
