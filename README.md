# 🏥 ClinAssist - Assistant Clinique Prédictif

## Vue d'ensemble

**ClinAssist** est une solution complète pour la planification intelligente des séances thérapeutiques avec des capacités prédictives basées sur l'IA et le Machine Learning (scikit-learn).

---

## 🏗️ Architecture Globale

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                            ClinAssist Platform                                   │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│   ┌──────────────────┐        HTTP/REST         ┌──────────────────────────┐   │
│   │    Angular 18    │ ◄──────────────────────► │     Spring Boot 3.2      │   │
│   │    Web App       │      (JSON + JWT)        │      Backend API         │   │
│   │   Port: 4200     │                          │      Port: 8080          │   │
│   └──────────────────┘                          └────────────┬─────────────┘   │
│                                                               │                  │
│                                                               │ HTTP/REST        │
│                                                               ▼                  │
│                                                  ┌──────────────────────────┐   │
│                                                  │    Flask ML Service      │   │
│                                                  │   (scikit-learn)         │   │
│                                                  │    Port: 5001            │   │
│                                                  └────────────┬─────────────┘   │
│                                                               │                  │
│                              ┌────────────────────────────────┘                  │
│                              │                                                   │
│                              ▼                                                   │
│                    ┌──────────────────────────┐                                  │
│                    │      PostgreSQL 15       │                                  │
│                    │       Database           │                                  │
│                    │       Port: 5433         │                                  │
│                    └──────────────────────────┘                                  │
└──────────────────────────────────────────────────────────────────────────────────┘
```

---

## 📡 Communication Frontend ↔ Backend

### Flux de Communication

```
┌─────────────┐     1. HTTP Request (GET/POST/PUT/DELETE)     ┌─────────────┐
│   Angular   │ ─────────────────────────────────────────────► │  Spring Boot │
│   Frontend  │                                                │   Backend    │
│             │     2. JSON Response + Status Code             │              │
│             │ ◄───────────────────────────────────────────── │              │
└─────────────┘                                                └─────────────┘

        │                                                              │
        │ 3. JWT Token (Bearer)                                        │
        │    inclus dans Header                                        │
        │    "Authorization: Bearer <token>"                           │
        └──────────────────────────────────────────────────────────────┘
```

### Authentification JWT

1. **Login** : `POST /api/auth/login` → Backend retourne un token JWT
2. **Requêtes sécurisées** : Token inclus dans le header `Authorization`
3. **Validation** : Backend vérifie le token à chaque requête

```typescript
// Angular - HttpInterceptor ajoute automatiquement le token
headers.set('Authorization', `Bearer ${this.authService.getToken()}`)
```

---

## 🔧 Backend - Spring Boot 3.2

### Structure du Projet

```
backend/
├── src/main/java/com/clinassist/
│   ├── controller/           # Contrôleurs REST API
│   │   ├── AuthController.java
│   │   ├── PatientController.java
│   │   ├── SeanceController.java
│   │   ├── PredictionController.java
│   │   └── DashboardController.java
│   │
│   ├── service/              # Logique métier
│   │   ├── PatientService.java
│   │   ├── SeanceService.java
│   │   ├── PredictionService.java      # Appelle le ML Service
│   │   ├── MLPredictionClient.java     # Client HTTP vers Flask
│   │   └── DashboardService.java
│   │
│   ├── repository/           # Accès base de données (JPA)
│   │   ├── PatientRepository.java
│   │   ├── SeanceRepository.java
│   │   └── PredictionRepository.java
│   │
│   ├── entity/               # Entités JPA (tables DB)
│   │   ├── Patient.java
│   │   ├── Seance.java
│   │   ├── Prediction.java
│   │   └── User.java
│   │
│   ├── dto/                  # Objets de transfert (JSON)
│   │   ├── PatientDTO.java
│   │   ├── SeanceDTO.java
│   │   └── PredictionDTO.java
│   │
│   ├── security/             # Configuration sécurité
│   │   ├── JwtTokenProvider.java
│   │   └── SecurityConfig.java
│   │
│   └── exception/            # Gestion des erreurs
│       └── GlobalExceptionHandler.java
│
├── src/main/resources/
│   └── application.yml       # Configuration
│
├── Dockerfile
└── pom.xml                   # Dépendances Maven
```

### Endpoints API Principaux

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `POST` | `/api/auth/login` | Authentification |
| `GET` | `/api/patients` | Liste des patients |
| `GET` | `/api/patients/{id}` | Détail d'un patient |
| `POST` | `/api/patients` | Créer un patient |
| `GET` | `/api/seances` | Liste des séances |
| `POST` | `/api/seances` | Planifier une séance |
| `GET` | `/api/dashboard/stats` | Statistiques dashboard |
| `POST` | `/api/predictions/patient/{id}/dropout-risk` | **Générer prédiction ML** |
| `PATCH` | `/api/predictions/{id}/reviewed` | Marquer comme traitée |

### Technologies Backend

- **Spring Boot 3.2** - Framework Java
- **Spring Security** - Authentification JWT
- **Spring Data JPA** - ORM pour PostgreSQL
- **Lombok** - Réduction du code boilerplate
- **Swagger/OpenAPI** - Documentation API (`/api/swagger-ui.html`)

---

## 🌐 Frontend - Angular 18

### Structure du Projet

```
web/src/app/
├── core/                     # Services et modèles partagés
│   ├── services/
│   │   ├── auth.service.ts
│   │   ├── patient.service.ts
│   │   ├── seance.service.ts
│   │   ├── prediction.service.ts
│   │   └── dashboard.service.ts
│   │
│   ├── models/
│   │   └── index.ts          # Interfaces TypeScript
│   │
│   └── guards/
│       └── auth.guard.ts     # Protection des routes
│
├── features/                 # Modules fonctionnels
│   ├── dashboard/            # Tableau de bord principal
│   │   └── dashboard.component.ts
│   │
│   ├── patients/             # Gestion des patients
│   │   ├── patient-list/
│   │   └── patient-detail/
│   │
│   ├── seances/              # Gestion des séances
│   │   └── seances.component.ts
│   │
│   └── predictions/          # Prédictions IA
│       └── predictions-dashboard/
│
├── shared/                   # Composants réutilisables
│   └── components/
│
└── app.routes.ts             # Configuration des routes
```

### Communication avec le Backend

```typescript
// patient.service.ts - Exemple de service Angular
@Injectable({ providedIn: 'root' })
export class PatientService {
  private apiUrl = 'http://localhost:8080/api/patients';

  constructor(private http: HttpClient) {}

  // GET - Récupérer tous les patients
  getPatients(): Observable<Patient[]> {
    return this.http.get<Patient[]>(this.apiUrl);
  }

  // POST - Créer un patient
  createPatient(patient: CreatePatientDTO): Observable<Patient> {
    return this.http.post<Patient>(this.apiUrl, patient);
  }

  // PUT - Mettre à jour un patient
  updatePatient(id: number, patient: UpdatePatientDTO): Observable<Patient> {
    return this.http.put<Patient>(`${this.apiUrl}/${id}`, patient);
  }
}
```

### Technologies Frontend

- **Angular 18** - Framework TypeScript
- **Standalone Components** - Architecture moderne sans NgModules
- **Angular Signals** - Gestion d'état réactive
- **HttpClient** - Requêtes HTTP vers le backend
- **RxJS** - Programmation réactive

---

## 🤖 Machine Learning - Flask + scikit-learn

### Architecture ML

```
┌─────────────────┐      HTTP POST      ┌─────────────────────────────────┐
│   Backend Java  │ ──────────────────► │     Flask ML Service            │
│                 │                     │                                  │
│ MLPrediction    │   JSON Request:     │   app.py                        │
│ Client.java     │   {                 │   ├── /api/predict/dropout-risk │
│                 │     cancellation_   │   ├── /api/predict/progress     │
│                 │     rate: 0.25,     │   └── /api/health               │
│                 │     no_show_rate:   │                                  │
│                 │     0.30, ...       │   Modèles scikit-learn:         │
│                 │   }                 │   ├── RandomForestRegressor     │
│                 │                     │   ├── GradientBoostingRegressor │
│                 │   JSON Response:    │   └── LinearRegression          │
│                 │ ◄────────────────── │                                  │
│                 │   {                 │   training/train_models.py      │
│                 │     risk_score: 52, │   └── Génère données synthét.   │
│                 │     risk_category:  │       et entraîne les modèles   │
│                 │     "HIGH",         │                                  │
│                 │     algorithm:      │   models/trained/               │
│                 │     "RandomForest"  │   ├── dropout_model.joblib      │
│                 │   }                 │   ├── progress_model.joblib     │
└─────────────────┘                     │   └── scheduler_model.joblib    │
                                        └─────────────────────────────────┘
```

### Algorithmes ML Utilisés

| Prédiction | Algorithme | Features | Sortie |
|------------|------------|----------|--------|
| **Risque d'abandon** | RandomForest | cancellation_rate, no_show_rate, days_since_last_session | Score 0-100% + Catégorie |
| **Progrès traitement** | GradientBoosting | avg_progress_rating, mood_improvement, completion_rate | Score + Recommandations |
| **Prochaine séance** | LinearRegression | avg_days_between, risk_level, progress_rating | Jours recommandés |

---

## 🐳 Docker - Déploiement

### Conteneurs

```yaml
# docker-compose.yml
services:
  postgres:        # Base de données - Port 5433
  backend:         # API Spring Boot - Port 8080
  web:             # Angular - Port 4200
  ml-service:      # Flask ML - Port 5001
  pgadmin:         # Admin DB - Port 5050
```

### Lancement

```bash
# Démarrer tous les services
docker-compose up -d

# Voir les logs
docker logs clinassist-backend -f

# Reconstruire après modifications
docker-compose build backend && docker-compose up -d backend
```

### URLs d'Accès

| Service | URL | Description |
|---------|-----|-------------|
| **Frontend Web** | http://localhost:4200 | Interface Angular |
| **Backend API** | http://localhost:8080/api | REST API |
| **Swagger UI** | http://localhost:8080/api/swagger-ui.html | Documentation API |
| **ML Service** | http://localhost:5001/api/health | Service ML |
| **pgAdmin** | http://localhost:5050 | Admin PostgreSQL |

---

## 📊 Fonctionnalités Implémentées

### Dashboard Principal
- ✅ Statistiques temps réel (patients, séances, risques)
- ✅ Graphique d'activité hebdomadaire
- ✅ Répartition des types de séances
- ✅ Alertes patients à risque

### Gestion des Patients
- ✅ Liste avec filtrage et recherche
- ✅ Fiche détaillée avec historique
- ✅ Score de risque ML affiché
- ✅ Génération de prédiction IA

### Prédictions IA
- ✅ Dashboard des prédictions ML
- ✅ Facteurs de risque avec valeurs
- ✅ Recommandations personnalisées
- ✅ Marquage "traité" persisté

### API REST Sécurisée
- ✅ Authentification JWT
- ✅ Endpoints CRUD complets
- ✅ Intégration ML microservice
- ✅ Validation des données

---

## 🔐 Identifiants de Test

| Utilisateur | Mot de passe | Rôle |
|-------------|--------------|------|
| dr.martin | test123 | THERAPEUTE |


---

## 📁 Fichiers Clés

| Fichier | Description |
|---------|-------------|
| `backend/src/.../PredictionService.java` | Logique ML côté backend |
| `backend/src/.../MLPredictionClient.java` | Client HTTP vers Flask |
| `web/src/app/core/services/prediction.service.ts` | Service Angular pour prédictions |
| `ml-service/app.py` | API Flask ML |
| `ml-service/training/train_models.py` | Script d'entraînement scikit-learn |

---


