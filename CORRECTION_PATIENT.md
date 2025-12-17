# Correction du problème "Ajouter Patient"

## Problème identifié
L'endpoint POST `/api/patients` était manquant dans le backend, ce qui causait une erreur 500 (Internal Server Error) lors de la tentative de création d'un nouveau patient.

## Corrections apportées

### 1. Création du DTO `PatientCreateRequest`
- **Fichier**: `backend/src/main/java/com/clinassist/dto/PatientCreateRequest.java`
- **Description**: DTO pour la création d'un nouveau patient avec validation des champs requis

### 2. Ajout de la méthode `createPatient` au `PatientService`
- **Fichier**: `backend/src/main/java/com/clinassist/service/PatientService.java`
- **Fonctionnalités**:
  - Création automatique d'un utilisateur associé au patient
  - Génération d'un nom d'utilisateur unique basé sur l'email
  - Génération d'un mot de passe temporaire
  - Attribution optionnelle d'un thérapeute
  - Validation de l'unicité de l'email

### 3. Ajout de l'endpoint POST au `PatientController`
- **Fichier**: `backend/src/main/java/com/clinassist/controller/PatientController.java`
- **Endpoint**: `POST /api/patients`
- **Sécurité**: Accessible aux rôles ADMIN, RECEPTIONIST, THERAPEUTE
- **Réponse**: HTTP 201 Created avec les détails du patient créé

## Comment tester

### Via l'interface web (localhost:4200)
1. Connectez-vous avec un compte thérapeute:
   - Email: `fatima.benali@clinassist.ma`
   - Password: `password123`

2. Allez dans la section "Patients"

3. Cliquez sur "Nouveau patient"

4. Remplissez le formulaire avec au minimum:
   - Prénom
   - Nom
   - Email (doit être unique)

5. Cliquez sur "Enregistrer"

### Via API (curl ou Postman)

```bash
# 1. Se connecter et obtenir un token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "fatima.benali@clinassist.ma",
    "password": "password123"
  }'

# 2. Utiliser le token pour créer un patient
curl -X POST http://localhost:8080/api/patients \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer VOTRE_TOKEN_ICI" \
  -d '{
    "email": "nouveau.patient@example.com",
    "firstName": "Ahmed",
    "lastName": "Alami",
    "phoneNumber": "0677-888888",
    "dateOfBirth": "1990-05-15",
    "gender": "MALE",
    "address": "123 Rue Example",
    "city": "Casablanca",
    "assignedTherapeuteId": 1
  }'
```

## Services Docker redémarrés

```bash
docker-compose stop backend
docker-compose build backend
docker-compose up -d backend
docker-compose restart web
```

## Vérification

Pour vérifier que le backend fonctionne:
```bash
docker logs clinassist-backend --tail 50
```

Le backend devrait afficher:
```
Started ClinAssistApplication in X seconds
```

## Prochaines étapes recommandées

1. **Envoi d'email**: Implémenter l'envoi d'un email avec le mot de passe temporaire au nouveau patient
2. **Validation avancée**: Ajouter des validations supplémentaires (format téléphone, etc.)
3. **Gestion d'erreurs**: Améliorer les messages d'erreur côté frontend pour afficher les vrais messages du backend
