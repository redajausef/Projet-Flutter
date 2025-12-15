# 🚀 Guide de Démarrage Rapide - ClinAssist

## Prérequis

- **Java 17+** (pour le backend)
- **Node.js 18+** (pour Angular)
- **Flutter 3.x** (pour mobile)
- **Docker** (optionnel, pour le déploiement)

---

## 🔧 Installation et Lancement

### 1. Backend Spring Boot

```bash
cd backend

# Lancer en mode développement (avec H2 database en mémoire)
./mvnw spring-boot:run

# Ou avec Maven installé
mvn spring-boot:run
```

Le backend sera accessible sur: **http://localhost:8080/api**

📖 Documentation API Swagger: **http://localhost:8080/api/swagger-ui.html**

### 2. Application Web Angular

```bash
cd web

# Installer les dépendances
npm install

# Lancer le serveur de développement
npm start
# ou
ng serve
```

L'application web sera accessible sur: **http://localhost:4200**

### 3. Application Mobile Flutter

```bash
cd mobile

# Installer les dépendances
flutter pub get

# Lancer sur un émulateur ou appareil connecté
flutter run

# Ou pour le web
flutter run -d chrome
```

---

## 🔐 Identifiants de Démonstration

| Rôle | Utilisateur | Mot de passe |
|------|------------|--------------|
| Admin | admin | admin123 |
| Thérapeute | dr.martin | password123 |
| Thérapeute | dr.dubois | password123 |
| Patient | marie.laurent | patient123 |

---

## 🐳 Déploiement Docker

```bash
# Lancer tous les services
docker-compose up -d

# Voir les logs
docker-compose logs -f

# Arrêter les services
docker-compose down
```

Services:
- **Backend**: http://localhost:8080
- **Web**: http://localhost:4200
- **PostgreSQL**: localhost:5432

---

## 📁 Structure du Projet

```
Projet-Flutter/
├── backend/                 # API Spring Boot
│   ├── src/main/java/      # Code source Java
│   └── pom.xml             # Dépendances Maven
│
├── mobile/                  # App Flutter
│   ├── lib/                # Code source Dart
│   └── pubspec.yaml        # Dépendances Flutter
│
├── web/                     # App Angular
│   ├── src/                # Code source TypeScript
│   └── package.json        # Dépendances npm
│
└── docker-compose.yml       # Orchestration Docker
```

---

## 🎨 Design System

### Couleurs Principales

| Nom | Hex | Usage |
|-----|-----|-------|
| Primary | #0D4F4F | Actions principales, boutons |
| Accent | #D4AF37 | Highlights, accents dorés |
| Background | #0A0E17 | Fond principal |
| Surface | #141A27 | Cartes, surfaces |
| Success | #10B981 | États positifs |
| Warning | #F59E0B | Alertes modérées |
| Error | #EF4444 | Erreurs, risques élevés |

---

## 📞 Support

Pour toute question ou problème:
- 📧 Email: support@clinassist.com
- 📖 Documentation complète dans README.md

---

**Bonne utilisation de ClinAssist! 🏥**

