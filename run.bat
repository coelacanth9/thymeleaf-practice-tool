@echo off
cd /d "%~dp0"
for %%f in (thymeleaf-practice-tool-*.jar) do start "Thymeleaf Practice Tool" java -jar "%%f" --spring.profiles.active=prod

:wait
timeout /t 1 /nobreak >nul
curl -s http://localhost:8080 >nul 2>&1
if errorlevel 1 goto wait

start http://localhost:8080
