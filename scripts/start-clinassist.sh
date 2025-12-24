#!/bin/bash
# ============================================================
# ClinAssist - Script de démarrage complet
# ============================================================
# Ce script initialise la base de données et démarre tous les services
# Usage: ./start-clinassist.sh

echo "=============================================="
echo "   ClinAssist - Démarrage de l'application"
echo "=============================================="

# 1. Démarrer tous les conteneurs Docker
echo ""
echo "[1/3] Démarrage des conteneurs Docker..."
docker-compose up -d

# 2. Attendre que PostgreSQL soit prêt
echo ""
echo "[2/3] Attente de la base de données..."
sleep 10

# Vérifier si la base est prête
until docker exec clinassist-db pg_isready -U clinassist -d clinassist > /dev/null 2>&1; do
    echo "   Attente de PostgreSQL..."
    sleep 2
done
echo "   PostgreSQL est prêt!"

# 3. Initialiser les données de démonstration (optionnel)
echo ""
echo "[3/3] Initialisation des données de démonstration..."
docker exec -i clinassist-db psql -U clinassist -d clinassist < docker/init-data.sql 2>/dev/null || echo "   Tables déjà initialisées"

echo ""
echo "=============================================="
echo "   ClinAssist est prêt!"
echo "=============================================="
echo ""
echo "   URLs disponibles:"
echo "   - Frontend Angular: http://localhost:4200"
echo "   - Backend API:      http://localhost:8080"
echo "   - pgAdmin:          http://localhost:5050"
echo "   - ML Service:       http://localhost:5001"
echo ""
echo "   Identifiants de connexion:"
echo "   - Email: sophie.martin@clinassist.com"
echo "   - Mot de passe: test123"
echo ""
echo "=============================================="
