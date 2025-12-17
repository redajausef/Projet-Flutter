# Guide d'intégration API Mobile - ClinAssist

## Résumé des changements

L'application mobile Flutter a été entièrement intégrée avec l'API backend pour éliminer toutes les données de simulation et afficher les vraies données de la base de données PostgreSQL.

## Architecture mise en place

### 📁 Structure des fichiers créés

```
mobile/lib/
├── core/
│   └── di/
│       └── app_bloc_providers.dart          # Configuration des BLoC providers
├── features/
│   ├── appointments/
│   │   ├── data/
│   │   │   ├── models/
│   │   │   │   └── seance_model.dart        # Modèle de données Seance
│   │   │   └── repositories/
│   │   │       └── seance_repository.dart    # Repository pour les séances
│   │   └── presentation/
│   │       └── bloc/
│   │           ├── seance_bloc.dart          # BLoC pour gérer l'état des séances
│   │           ├── seance_event.dart         # Événements
│   │           └── seance_state.dart         # États
│   ├── home/
│   │   ├── data/
│   │   │   ├── models/
│   │   │   │   └── patient_stats_model.dart # Modèle de statistiques patient
│   │   │   └── repositories/
│   │   │       └── patient_repository.dart   # Repository pour les patients
│   │   └── presentation/
│   │       └── bloc/
│   │           ├── home_bloc.dart            # BLoC pour la page d'accueil
│   │           ├── home_event.dart           # Événements
│   │           └── home_state.dart           # États
│   └── predictions/
│       ├── data/
│       │   ├── models/
│       │   │   └── prediction_model.dart     # Modèle de prédictions
│       │   └── repositories/
│       │       └── prediction_repository.dart # Repository pour les prédictions
│       └── presentation/
│           └── bloc/
│               ├── prediction_bloc.dart      # BLoC pour les prédictions
│               ├── prediction_event.dart     # Événements
│               └── prediction_state.dart     # États
```

### 📦 Fichiers modifiés

1. **home_page.dart** - Intégration avec HomeBloc et SeanceBloc pour afficher les vraies statistiques
2. **appointments_page.dart** - Intégration avec SeanceBloc pour afficher les vraies séances

## Endpoints API utilisés

### 1. Patients (Home)
```
GET /api/patients/{id}
```
- Récupère les informations du patient
- Utilisé pour afficher les statistiques (totalSeances, completedSeances, riskScore, etc.)

### 2. Séances (Appointments)
```
GET /api/seances/patient/{patientId}
```
- Récupère toutes les séances d'un patient
- Utilisé pour afficher le calendrier et la liste des rendez-vous

### 3. Prédictions
```
GET /api/predictions/patient/{patientId}
POST /api/predictions/patient/{patientId}/next-session
POST /api/predictions/patient/{patientId}/dropout-risk
```
- Récupère les prédictions existantes
- Génère de nouvelles prédictions

## Modèles de données

### SeanceModel
```dart
class SeanceModel {
  final int id;
  final String seanceCode;
  final int patientId;
  final String? patientName;
  final int therapeuteId;
  final String? therapeuteName;
  final String type;
  final String status;
  final DateTime scheduledAt;
  final int durationMinutes;
  final String? notes;
  final bool isVideoSession;
  final DateTime createdAt;
}
```

### PredictionModel
```dart
class PredictionModel {
  final int id;
  final int patientId;
  final String? patientName;
  final String type;
  final double score;
  final double confidence;
  final String riskLevel;
  final String? recommendation;
  final List<String>? factors;
  final DateTime generatedAt;
  final bool isActive;
}
```

### PatientStatsModel
```dart
class PatientStatsModel {
  final int totalSeances;
  final int completedSeances;
  final int upcomingSeances;
  final double? riskScore;
  final String? riskCategory;
  final double? progressPercentage;
  
  double get completionRate; // Calculé automatiquement
}
```

## Gestion de l'état avec BLoC

### HomeBloc
**États :**
- `HomeInitial` - État initial
- `HomeLoading` - Chargement des données
- `HomeLoaded(stats)` - Données chargées avec statistiques
- `HomeError(message)` - Erreur

**Événements :**
- `LoadPatientStats(patientId, token)` - Charger les statistiques du patient

### SeanceBloc
**États :**
- `SeanceInitial` - État initial
- `SeanceLoading` - Chargement des séances
- `SeancesLoaded(seances)` - Séances chargées
- `SeanceError(message)` - Erreur

**Événements :**
- `LoadPatientSeances(patientId, token)` - Charger toutes les séances
- `LoadUpcomingSeances(patientId, token)` - Charger seulement les séances à venir

### PredictionBloc
**États :**
- `PredictionInitial` - État initial
- `PredictionLoading` - Chargement/génération
- `PredictionsLoaded(predictions)` - Prédictions chargées
- `PredictionGenerated(prediction)` - Nouvelle prédiction générée
- `PredictionError(message)` - Erreur

**Événements :**
- `LoadPatientPredictions(patientId, token)` - Charger les prédictions
- `GenerateNextSessionPrediction(patientId, token)` - Générer prédiction session
- `GenerateDropoutRiskPrediction(patientId, token)` - Générer prédiction abandon

## Configuration réseau

### URL de base
```dart
final String baseUrl = 'http://10.0.2.2:8080/api';
```

**Note :** `10.0.2.2` est l'adresse localhost pour l'émulateur Android. Pour un appareil physique, remplacer par l'IP de la machine hôte.

### Configuration Dio
```dart
Dio(BaseOptions(
  baseUrl: 'http://10.0.2.2:8080/api',
  connectTimeout: const Duration(seconds: 5),
  receiveTimeout: const Duration(seconds: 3),
))
```

## Utilisation dans les pages

### Page d'accueil (HomePage)

```dart
@override
void initState() {
  super.initState();
  final authState = context.read<AuthBloc>().state;
  if (authState is Authenticated) {
    final patientId = authState.user.patientId;
    final token = authState.token;
    
    context.read<HomeBloc>().add(LoadPatientStats(patientId!, token));
    context.read<SeanceBloc>().add(LoadUpcomingSeances(patientId, token));
  }
}
```

### Affichage avec BlocBuilder

```dart
BlocBuilder<HomeBloc, HomeState>(
  builder: (context, state) {
    if (state is HomeLoading) {
      return CircularProgressIndicator();
    } else if (state is HomeLoaded) {
      return StatCard(
        title: 'Séances',
        value: '${state.stats.totalSeances}',
        ...
      );
    } else if (state is HomeError) {
      return ErrorWidget(state.message);
    }
    return SizedBox();
  },
)
```

## Données affichées

### Page d'accueil
- ✅ Nombre total de séances (depuis l'API)
- ✅ Nombre de séances complétées (depuis l'API)
- ✅ Taux de complétion calculé
- ✅ Score de risque (si disponible)
- ✅ Catégorie de risque
- ✅ Liste des prochaines séances (2 premières)

### Page Appointments
- ✅ Liste complète des séances du patient
- ✅ Informations du thérapeute
- ✅ Date et heure
- ✅ Type de séance (vidéo/en personne)
- ✅ Statut (SCHEDULED, COMPLETED, CANCELLED)
- ✅ Calendrier avec séances

### Page Predictions
- ⏳ Liste des prédictions (à implémenter)
- ⏳ Génération de nouvelles prédictions (à implémenter)
- ⏳ Graphiques de progression (à implémenter)

## Prochaines étapes

### 1. Finaliser la page Predictions
- Mettre à jour `predictions_page.dart` pour utiliser `PredictionBloc`
- Afficher les prédictions depuis l'API
- Implémenter la génération de prédictions

### 2. Ajouter l'authentification complète
- Mettre à jour `AuthBloc` pour utiliser `AuthRepository`
- Gérer la connexion JWT
- Stocker le token avec `flutter_secure_storage`

### 3. Améliorer la gestion des erreurs
- Messages d'erreur personnalisés
- Retry automatique
- Mode offline avec cache

### 4. Optimisations
- Cache des données
- Pull-to-refresh
- Pagination pour les longues listes
- Loading skeletons

### 5. Tests
- Tests unitaires pour les BLoCs
- Tests d'intégration pour les repositories
- Tests de widgets

## Dépannage

### Erreur de connexion
**Problème :** `Exception: Erreur de connexion: Failed to connect`

**Solutions :**
1. Vérifier que le backend est démarré (`docker-compose up`)
2. Vérifier l'URL (`10.0.2.2` pour émulateur)
3. Vérifier que le port 8080 n'est pas bloqué par le firewall

### Données non chargées
**Problème :** Les données ne s'affichent pas

**Solutions :**
1. Vérifier les logs : `flutter logs`
2. Vérifier que le patientId est correct
3. Vérifier que l'utilisateur connecté a un patientId
4. Tester l'API avec curl/Postman

### BLoC non trouvé
**Problème :** `BlocProvider.of() called with a context that does not contain a Bloc`

**Solutions :**
1. Vérifier que `AppBlocProviders` entoure l'application
2. Vérifier l'arbre des widgets
3. Utiliser `context.read<>()` au lieu de `context.watch<>()`

## État actuel

### ✅ Terminé
- Architecture BLoC complète
- Modèles de données
- Repositories pour API
- Intégration HomePage
- Intégration AppointmentsPage (partielle)
- Configuration des providers

### ⏳ En cours
- Finalisation AppointmentsPage
- Tests des intégrations

### ❌ À faire
- Page Predictions
- Authentification JWT complète
- Gestion du cache
- Tests automatisés

## Commandes utiles

```bash
# Lancer l'app en mode debug
flutter run

# Voir les logs
flutter logs

# Rebuild l'app
flutter clean && flutter pub get && flutter run

# Tester l'API backend
curl http://localhost:8080/api/patients/1

# Vérifier les containers Docker
docker-compose ps
docker-compose logs backend
```

## Support

Pour toute question :
1. Consulter les logs de l'app : `flutter logs`
2. Vérifier les logs backend : `docker-compose logs backend`
3. Tester les endpoints avec curl/Postman
4. Vérifier la documentation de l'API : http://localhost:8080/swagger-ui.html
