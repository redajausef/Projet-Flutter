# Guide d'inscription des patients - Application Mobile

## Vue d'ensemble

L'application mobile ClinAssist permet maintenant aux nouveaux patients de s'inscrire directement depuis l'application. Le processus d'inscription est divisé en deux étapes :

1. **Formulaire d'inscription** : Le patient remplit ses informations personnelles
2. **Sélection du thérapeute** : Le patient choisit son thérapeute parmi la liste disponible

## Architecture

### Structure des fichiers

```
mobile/lib/features/auth/
├── data/
│   ├── models/
│   │   └── therapeute_model.dart      # Modèle de données pour un thérapeute
│   └── repositories/
│       └── auth_repository.dart        # Repository pour l'authentification et l'inscription
├── presentation/
│   ├── bloc/
│   │   ├── register_bloc.dart         # Logique métier de l'inscription
│   │   ├── register_event.dart        # Événements d'inscription
│   │   └── register_state.dart        # États d'inscription
│   └── pages/
│       ├── register_patient_page.dart # Page de formulaire d'inscription
│       └── select_therapeute_page.dart # Page de sélection du thérapeute
```

### Flux de données

```
RegisterPatientPage
    ↓ (formulaire validé)
SelectTherapeutePage
    ↓ (chargement des thérapeutes)
RegisterBloc → LoadTherapeutes
    ↓
AuthRepository → GET /api/therapeutes
    ↓ (thérapeute sélectionné + confirmation)
RegisterBloc → RegisterPatient
    ↓
AuthRepository → POST /api/patients
    ↓ (succès)
Navigation → LoginPage
```

## Fonctionnalités

### 1. Page d'inscription (`RegisterPatientPage`)

**Étapes du formulaire :**

- **Étape 1 : Informations personnelles**
  - Prénom (requis)
  - Nom (requis)
  - Email (requis, validé)
  - Téléphone (requis)

- **Étape 2 : Informations complémentaires**
  - Date de naissance (requis, sélection avec date picker)
  - Genre (requis, dropdown : Homme/Femme/Autre)
  - Adresse (optionnel)

- **Étape 3 : Sécurité**
  - Mot de passe (requis, minimum 6 caractères)
  - Confirmation du mot de passe (requis, doit correspondre)

**Validation :**
- Email valide
- Téléphone non vide
- Date de naissance sélectionnée
- Mot de passe minimum 6 caractères
- Correspondance des mots de passe

### 2. Page de sélection du thérapeute (`SelectTherapeutePage`)

**Fonctionnalités :**
- Chargement automatique des thérapeutes disponibles depuis l'API
- Affichage des informations du thérapeute :
  - Nom complet
  - Spécialité
  - Note (si disponible)
  - Années d'expérience (si disponible)
  - Bio (si disponible)
- Sélection radio pour choisir un thérapeute
- Indicateur de chargement pendant l'inscription
- Gestion des erreurs avec possibilité de réessayer

## API Endpoints utilisés

### GET `/api/therapeutes`

Récupère la liste de tous les thérapeutes disponibles.

**Réponse :**
```json
[
  {
    "id": 1,
    "firstName": "Sophie",
    "lastName": "Martin",
    "specialty": "Psychologie clinique",
    "bio": "Spécialisée en TCC",
    "rating": 4.8,
    "yearsExperience": 9
  }
]
```

### POST `/api/patients`

Crée un nouveau patient.

**Requête :**
```json
{
  "email": "patient@example.com",
  "firstName": "Jean",
  "lastName": "Dupont",
  "phoneNumber": "0612345678",
  "dateOfBirth": "1990-05-15",
  "gender": "MALE",
  "address": "123 Rue de Paris",
  "assignedTherapeuteId": 1
}
```

**Réponse :**
```json
{
  "id": 7,
  "email": "patient@example.com",
  "firstName": "Jean",
  "lastName": "Dupont",
  "phoneNumber": "0612345678",
  "dateOfBirth": "1990-05-15",
  "gender": "MALE",
  "address": "123 Rue de Paris",
  "assignedTherapeute": {
    "id": 1,
    "firstName": "Sophie",
    "lastName": "Martin"
  }
}
```

## Gestion de l'état avec BLoC

### RegisterBloc

**Événements :**
- `LoadTherapeutes` : Charge la liste des thérapeutes disponibles
- `RegisterPatient` : Enregistre un nouveau patient

**États :**
- `RegisterInitial` : État initial
- `TherapeutesLoading` : Chargement des thérapeutes
- `TherapeutesLoaded` : Thérapeutes chargés avec succès
- `TherapeutesError` : Erreur lors du chargement des thérapeutes
- `RegisterLoading` : Inscription en cours
- `RegisterSuccess` : Inscription réussie
- `RegisterFailure` : Erreur lors de l'inscription

## Configuration réseau

### Android Emulator

L'URL de base utilisée est `http://10.0.2.2:8080/api` qui correspond à `localhost` sur la machine hôte.

### Appareil physique

Pour tester sur un appareil physique, vous devez :
1. Vous assurer que l'appareil et le serveur backend sont sur le même réseau
2. Remplacer `10.0.2.2` par l'adresse IP locale de votre machine (ex: `192.168.1.10`)

## Utilisation

### 1. Depuis la page de connexion

Un lien "S'inscrire" est disponible en bas de la page de connexion :

```dart
TextButton(
  onPressed: () {
    context.go('/register');
  },
  child: const Text('S\'inscrire'),
),
```

### 2. Navigation dans le router

Les routes suivantes ont été ajoutées dans `app_router.dart` :

```dart
GoRoute(
  path: '/register',
  builder: (context, state) => const RegisterPatientPage(),
),
GoRoute(
  path: '/select-therapeute',
  builder: (context, state) {
    final patientData = state.extra as Map<String, dynamic>;
    return SelectTherapeutePage(patientData: patientData);
  },
),
```

## Test du flux complet

1. **Démarrer le backend** : Assurez-vous que le backend est démarré sur `http://localhost:8080`
2. **Lancer l'app mobile** : `flutter run` dans le dossier `mobile/`
3. **Aller à la page d'inscription** : Cliquer sur "S'inscrire" depuis la page de connexion
4. **Remplir le formulaire** :
   - Étape 1 : Entrer prénom, nom, email, téléphone
   - Étape 2 : Sélectionner date de naissance, genre, adresse (optionnel)
   - Étape 3 : Entrer et confirmer le mot de passe
5. **Sélectionner un thérapeute** : Choisir dans la liste
6. **Confirmer** : Cliquer sur "Confirmer"
7. **Connexion** : Une fois inscrit, se connecter avec l'email et le mot de passe

## Améliorations futures

- [ ] Ajouter la validation de l'email côté backend (vérification de domaine)
- [ ] Implémenter un système d'approbation par le thérapeute
- [ ] Ajouter la possibilité de télécharger une photo de profil
- [ ] Envoyer un email de confirmation après l'inscription
- [ ] Permettre de changer de thérapeute après l'inscription
- [ ] Ajouter une recherche/filtre dans la liste des thérapeutes
- [ ] Implémenter la récupération de mot de passe
- [ ] Ajouter l'authentification biométrique (Face ID / Touch ID)

## Dépannage

### Erreur de connexion

Si vous obtenez une erreur de connexion :
1. Vérifiez que le backend est bien démarré
2. Vérifiez l'URL dans `AuthRepository` (10.0.2.2 pour émulateur)
3. Vérifiez les logs du backend pour voir si la requête arrive

### Thérapeutes non chargés

Si la liste des thérapeutes ne se charge pas :
1. Vérifiez que l'endpoint `/api/therapeutes` fonctionne (tester avec curl ou Postman)
2. Vérifiez les logs de l'application mobile
3. Assurez-vous qu'il y a au moins un thérapeute dans la base de données

### Erreur d'inscription

Si l'inscription échoue :
1. Vérifiez les logs du backend pour voir l'erreur exacte
2. Assurez-vous que l'email n'est pas déjà utilisé
3. Vérifiez que tous les champs requis sont remplis
4. Vérifiez que le format de la date de naissance est correct (YYYY-MM-DD)

## Support

Pour toute question ou problème, consultez :
- Les logs de l'application : `flutter logs`
- Les logs du backend : Docker logs du container `clinassist-backend`
- La documentation de l'API : `http://localhost:8080/swagger-ui.html`
