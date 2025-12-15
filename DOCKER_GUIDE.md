# 🐳 Guide Docker - ClinAssist

## Prérequis

- **Docker Desktop** installé et lancé
- **Aucune installation locale** de PostgreSQL nécessaire !

---

## 🚀 Démarrage Rapide

### Windows
```cmd
cd Projet-Flutter
scripts\start-docker.bat
```

### Linux/Mac
```bash
cd Projet-Flutter
chmod +x scripts/start-docker.sh
./scripts/start-docker.sh
```

### Ou manuellement
```bash
# Construire et lancer tous les services
docker-compose up -d --build

# Voir les logs en temps réel
docker-compose logs -f
```

---

## 📦 Services Docker

| Service | Port | Description |
|---------|------|-------------|
| **postgres** | 5432 | Base de données PostgreSQL |
| **backend** | 8080 | API Spring Boot |
| **web** | 4200 | Application Angular |
| **pgadmin** | 5050 | Interface admin PostgreSQL |

---

## 🔗 URLs d'accès

| Application | URL |
|-------------|-----|
| 🌐 Web App | http://localhost:4200 |
| 🔧 API Backend | http://localhost:8080/api |
| 📖 Swagger UI | http://localhost:8080/api/swagger-ui.html |
| 🗄️ pgAdmin | http://localhost:5050 |

---

## 🔐 Identifiants

### Application ClinAssist
| Rôle | Utilisateur | Mot de passe |
|------|-------------|--------------|
| Admin | admin | admin123 |
| Thérapeute | dr.martin | password123 |
| Patient | marie.laurent | patient123 |

### pgAdmin
- **Email**: admin@clinassist.com
- **Mot de passe**: admin123

### Connexion PostgreSQL (depuis pgAdmin)
- **Host**: postgres (ou localhost si hors Docker)
- **Port**: 5432
- **Database**: clinassist
- **Username**: clinassist
- **Password**: clinassist_secret_2024

---

## 📋 Commandes Utiles

### Gestion des conteneurs
```bash
# Démarrer tous les services
docker-compose up -d

# Arrêter tous les services
docker-compose down

# Voir les conteneurs en cours
docker-compose ps

# Voir les logs
docker-compose logs -f

# Logs d'un service spécifique
docker-compose logs -f backend
docker-compose logs -f postgres
```

### Rebuild après modifications
```bash
# Reconstruire un service spécifique
docker-compose build backend
docker-compose up -d backend

# Reconstruire tout
docker-compose up -d --build
```

### Base de données
```bash
# Accéder au shell PostgreSQL
docker exec -it clinassist-db psql -U clinassist -d clinassist

# Backup de la base
docker exec clinassist-db pg_dump -U clinassist clinassist > backup.sql

# Restaurer un backup
docker exec -i clinassist-db psql -U clinassist clinassist < backup.sql
```

### Nettoyage
```bash
# Supprimer les conteneurs et volumes
docker-compose down -v

# Nettoyer tout Docker (attention!)
docker system prune -a
```

---

## 🔧 Développement avec Docker

### Option 1: Tout dans Docker (recommandé pour tester)
```bash
docker-compose up -d
```
Accédez à http://localhost:4200

### Option 2: Seulement PostgreSQL dans Docker
```bash
# Démarrer uniquement la base de données
docker-compose -f docker/docker-compose.dev.yml up -d

# Puis lancer le backend localement
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=docker

# Et le frontend
cd web
npm start
```

---

## 🐛 Dépannage

### Le backend ne démarre pas
```bash
# Vérifier que PostgreSQL est prêt
docker-compose logs postgres

# Redémarrer le backend
docker-compose restart backend
```

### Erreur de connexion à la base
```bash
# Vérifier la santé de PostgreSQL
docker-compose exec postgres pg_isready -U clinassist

# Recréer le conteneur
docker-compose down -v
docker-compose up -d
```

### Port déjà utilisé
```bash
# Changer les ports dans docker-compose.yml
# Exemple: "8081:8080" au lieu de "8080:8080"
```

### Voir les logs en temps réel
```bash
docker-compose logs -f --tail=100
```

---

## 📊 Architecture Docker

```
┌─────────────────────────────────────────────────────────────┐
│                    Docker Network                            │
│                  (clinassist-network)                        │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐              │
│  │   Web    │    │ Backend  │    │ Postgres │              │
│  │  :4200   │───▶│  :8080   │───▶│  :5432   │              │
│  │ (nginx)  │    │ (Spring) │    │          │              │
│  └──────────┘    └──────────┘    └──────────┘              │
│       │                               ▲                     │
│       │         ┌──────────┐          │                     │
│       │         │ pgAdmin  │──────────┘                     │
│       │         │  :5050   │                                │
│       │         └──────────┘                                │
│       ▼                                                      │
│   Navigateur                                                 │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## ✅ Vérification de l'installation

1. Ouvrez http://localhost:4200 → Page de login
2. Connectez-vous avec `admin` / `admin123`
3. Vous devriez voir le dashboard
4. Testez l'API: http://localhost:8080/api/swagger-ui.html

**C'est tout ! Aucune installation locale requise ! 🎉**

