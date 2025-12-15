@echo off
echo ==========================================
echo    ClinAssist - Docker Startup Script
echo ==========================================
echo.

echo [1/4] Stopping existing containers...
docker-compose down

echo.
echo [2/4] Building images...
docker-compose build --no-cache

echo.
echo [3/4] Starting all services...
docker-compose up -d

echo.
echo [4/4] Waiting for services to be ready...
timeout /t 30 /nobreak > nul

echo.
echo ==========================================
echo    ClinAssist is now running!
echo ==========================================
echo.
echo Services available at:
echo   - Web App:     http://localhost:4200
echo   - Backend API: http://localhost:8080/api
echo   - Swagger UI:  http://localhost:8080/api/swagger-ui.html
echo   - pgAdmin:     http://localhost:5050
echo.
echo Demo credentials:
echo   - Admin: admin / admin123
echo   - Therapist: dr.martin / password123
echo.
echo To view logs: docker-compose logs -f
echo To stop: docker-compose down
echo.
pause

