@echo off
REM ============================================================
REM ClinAssist - Script de démarrage complet (Windows)
REM ============================================================
REM Ce script initialise la base de données et démarre tous les services
REM Usage: start-clinassist.bat

echo ==============================================
echo    ClinAssist - Demarrage de l'application
echo ==============================================

REM 1. Démarrer tous les conteneurs Docker
echo.
echo [1/3] Demarrage des conteneurs Docker...
docker-compose up -d

REM 2. Attendre que PostgreSQL soit prêt
echo.
echo [2/3] Attente de la base de donnees (15 secondes)...
timeout /t 15 /nobreak > nul

REM 3. Initialiser les données de démonstration
echo.
echo [3/3] Initialisation des donnees de demonstration...
docker exec -i clinassist-db psql -U clinassist -d clinassist -f /docker-entrypoint-initdb.d/init.sql 2>nul
echo    Tables initialisees!

echo.
echo ==============================================
echo    ClinAssist est pret!
echo ==============================================
echo.
echo    URLs disponibles:
echo    - Frontend Angular: http://localhost:4200
echo    - Backend API:      http://localhost:8080
echo    - pgAdmin:          http://localhost:5050
echo    - ML Service:       http://localhost:5001
echo.
echo    Identifiants de connexion:
echo    - Email: sophie.martin@clinassist.com
echo    - Mot de passe: test123
echo.
echo ==============================================
pause
