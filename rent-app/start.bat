@echo off
chcp 65001 >nul
echo Rent House Website Startup Script
echo ===================================

echo Starting User Service (Port 8081)...
start "User Service" cmd /c "cd user-service && mvn spring-boot:run"

timeout /t 10 /nobreak >nul

echo Starting House Service (Port 8082)...
start "House Service" cmd /c "cd house-service && mvn spring-boot:run"

timeout /t 10 /nobreak >nul

echo Starting Gateway Service (Port 8080)...
start "Gateway Service" cmd /c "cd gateway && mvn spring-boot:run"

echo.
echo All services started!
echo Please wait about 30 seconds for services to fully start
echo Then open index.html for testing
echo.
pause