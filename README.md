# 🏥 ClinAssist - Assistant Clinique Prédictif

## Vue d'ensemble

**ClinAssist** est une solution complète pour la planification intelligente des séances thérapeutiques avec des capacités prédictives basées sur l'IA.

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    ClinAssist Platform                       │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│   ┌──────────────┐   ┌──────────────┐   ┌──────────────┐   │
│   │   Angular    │   │    Flutter   │   │  Spring Boot │   │
│   │   Web App    │   │  Mobile App  │   │   Backend    │   │
│   │   (Admin)    │   │  (Patients)  │   │    (API)     │   │
│   └──────┬───────┘   └──────┬───────┘   └──────┬───────┘   │
│          │                  │                  │            │
│          └──────────────────┼──────────────────┘            │
│                             │                               │
│                    ┌────────▼────────┐                      │
│                    │   PostgreSQL    │                      │
│                    │   Database      │                      │
│                    └─────────────────┘                      │
└─────────────────────────────────────────────────────────────┘
```

## 📁 Structure du Projet

```
Projet-Flutter/
├── backend/                 # Spring Boot Java API
│   ├── src/main/java/
│   ├── src/main/resources/
│   └── pom.xml
│
├── mobile/                  # Flutter Mobile App
│   ├── lib/
│   ├── android/
│   ├── ios/
│   └── pubspec.yaml
│
├── web/                     # Angular Web App
│   ├── src/
│   ├── angular.json
│   └── package.json
│
└── docker-compose.yml       # Orchestration
```

## 🚀 Fonctionnalités

### Backend (Spring Boot)
- ✅ API REST sécurisée avec JWT
- ✅ Gestion des patients et thérapeutes
- ✅ Planification des séances
- ✅ Algorithme prédictif pour recommandations
- ✅ Notifications et rappels

### Mobile (Flutter)
- ✅ Interface patient élégante
- ✅ Prise de rendez-vous
- ✅ Historique des séances
- ✅ Notifications push
- ✅ Mode hors-ligne

### Web (Angular)
- ✅ Dashboard administrateur
- ✅ Gestion complète des patients
- ✅ Calendrier interactif
- ✅ Analyses et rapports
- ✅ Gestion des thérapeutes

## 🛠️ Installation

### Prérequis
- Java 17+
- Node.js 18+
- Flutter 3.x
- PostgreSQL 15+
- Docker (optionnel)

### Lancement rapide avec Docker

```bash
docker-compose up -d
```

### Lancement manuel

**Backend:**
```bash
cd backend
./mvnw spring-boot:run
```

**Mobile:**
```bash
cd mobile
flutter pub get
flutter run
```

**Web:**
```bash
cd web
npm install
ng serve
```

## 🎨 Design System

Le projet utilise un design system cohérent avec:
- **Couleur principale:** Deep Teal (#0D4F4F)
- **Accent:** Gold (#D4AF37)
- **Fond sombre:** #0A0E17
- **Typographie:** Inter, SF Pro Display

## 📄 License

MIT License - Voir [LICENSE](LICENSE) pour plus de détails.

