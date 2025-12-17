-- ============================================
-- ClinAssist - Données de démonstration
-- Noms marocains
-- ============================================

-- Nettoyer les données existantes (dans l'ordre pour respecter les contraintes)
DELETE FROM prediction_factors WHERE prediction_id > 0;
DELETE FROM predictions WHERE id > 0;
DELETE FROM seances WHERE id > 0;
DELETE FROM disponibilite_slots WHERE id > 0;
DELETE FROM notifications WHERE id > 0;
DELETE FROM patients WHERE id > 0;
DELETE FROM therapeute_languages WHERE therapeute_id > 0;
DELETE FROM therapeute_specialties WHERE therapeute_id > 0;
DELETE FROM therapeutes WHERE id > 0;
DELETE FROM users WHERE id > 0;

-- ============================================
-- UTILISATEURS (Admin + Thérapeutes + Patients)
-- Password for admin: admin123
-- Password for others: password123
-- BCrypt hash for admin123: $2a$10$IrLjMKRrRRJmvPrCOYJPOuB7AFXU7LmXgqVF3vb7n1fhQxPgL6hWy
-- BCrypt hash for password123: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi
-- ============================================
INSERT INTO users (id, username, email, password, first_name, last_name, phone_number, role, is_active, is_email_verified, created_at, updated_at)
VALUES
-- Admin
(1, 'admin', 'admin@clinassist.com', '$2a$10$IrLjMKRrRRJmvPrCOYJPOuB7AFXU7LmXgqVF3vb7n1fhQxPgL6hWy', 'Admin', 'System', '0660-000000', 'ADMIN', true, true, NOW(), NOW()),
-- Thérapeutes
(2, 'dr.benali', 'fatima.benali@clinassist.ma', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Fatima', 'Benali', '0661-234567', 'THERAPEUTE', true, true, NOW(), NOW()),
(3, 'dr.alaoui', 'youssef.alaoui@clinassist.ma', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Youssef', 'Alaoui', '0662-345678', 'THERAPEUTE', true, true, NOW(), NOW()),
(4, 'dr.hassani', 'amina.hassani@clinassist.ma', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Amina', 'Hassani', '0663-456789', 'THERAPEUTE', true, true, NOW(), NOW()),
(5, 'dr.tazi', 'omar.tazi@clinassist.ma', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Omar', 'Tazi', '0664-567890', 'THERAPEUTE', true, true, NOW(), NOW()),

-- Patients
(6, 'sara.ouazzani', 'sara.ouazzani@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Sara', 'Ouazzani', '0665-123456', 'PATIENT', true, true, NOW(), NOW()),
(7, 'karim.benjelloun', 'karim.benjelloun@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Karim', 'Benjelloun', '0666-234567', 'PATIENT', true, true, NOW(), NOW()),
(8, 'leila.cherkaoui', 'leila.cherkaoui@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Leila', 'Cherkaoui', '0667-345678', 'PATIENT', true, true, NOW(), NOW()),
(9, 'mehdi.fassi', 'mehdi.fassi@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Mehdi', 'Fassi Fihri', '0668-456789', 'PATIENT', true, true, NOW(), NOW()),
(10, 'nadia.berrada', 'nadia.berrada@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Nadia', 'Berrada', '0669-567890', 'PATIENT', true, true, NOW(), NOW()),
(11, 'amine.lahlou', 'amine.lahlou@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Amine', 'Lahlou', '0670-678901', 'PATIENT', true, true, NOW(), NOW()),
(12, 'zineb.idrissi', 'zineb.idrissi@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Zineb', 'Idrissi', '0671-789012', 'PATIENT', true, true, NOW(), NOW()),
(13, 'rachid.bennani', 'rachid.bennani@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Rachid', 'Bennani', '0672-890123', 'PATIENT', true, true, NOW(), NOW()),
(14, 'kenza.elamrani', 'kenza.elamrani@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Kenza', 'El Amrani', '0673-901234', 'PATIENT', true, true, NOW(), NOW()),
(15, 'yassine.kettani', 'yassine.kettani@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Yassine', 'Kettani', '0674-012345', 'PATIENT', true, true, NOW(), NOW()),
(16, 'hajar.squalli', 'hajar.squalli@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Hajar', 'Squalli', '0675-123456', 'PATIENT', true, true, NOW(), NOW()),
(17, 'hamza.chraibi', 'hamza.chraibi@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Hamza', 'Chraibi', '0676-234567', 'PATIENT', true, true, NOW(), NOW());

SELECT setval('users_id_seq', 17);

-- ============================================
-- THERAPEUTES
-- ============================================
INSERT INTO therapeutes (id, user_id, therapeute_code, specialization, license_number, years_of_experience, biography, status, rating, total_reviews, consultation_fee, currency, created_at, updated_at)
VALUES
(1, 2, 'THER-001', 'Psychologie clinique', 'PSY-MA-2015-001', 9, 'Spécialiste en thérapie cognitive comportementale. Formée à l''Université Mohammed V de Rabat. Expertise dans le traitement de l''anxiété et de la dépression.', 'AVAILABLE', 4.8, 45, 500.00, 'MAD', NOW(), NOW()),
(2, 3, 'THER-002', 'Psychiatrie', 'PSY-MA-2012-042', 12, 'Psychiatre spécialisé dans les troubles de l''humeur. Chef de service au CHU Ibn Sina de Rabat.', 'AVAILABLE', 4.9, 38, 600.00, 'MAD', NOW(), NOW()),
(3, 4, 'THER-003', 'Psychothérapie familiale', 'PSY-MA-2018-089', 6, 'Thérapeute familiale et de couple, approche systémique. Formation à l''Institut de Thérapie Familiale de Casablanca.', 'AVAILABLE', 4.7, 25, 450.00, 'MAD', NOW(), NOW()),
(4, 5, 'THER-004', 'Neuropsychologie', 'PSY-MA-2016-056', 8, 'Neuropsychologue spécialisé dans l''évaluation et la réhabilitation cognitive. CHU Hassan II de Fès.', 'ON_LEAVE', 4.6, 30, 550.00, 'MAD', NOW(), NOW());

SELECT setval('therapeutes_id_seq', 4);

-- ============================================
-- PATIENTS
-- ============================================
INSERT INTO patients (id, user_id, patient_code, date_of_birth, gender, address, city, postal_code, country, emergency_contact_name, emergency_contact_phone, medical_history, status, assigned_therapeute_id, risk_score, risk_category, created_at, updated_at)
VALUES
(1, 6, 'PAT-001', '1992-03-15', 'FEMALE', '25 Avenue Hassan II', 'Casablanca', '20000', 'Maroc', 'Ahmed Ouazzani', '0661-111111', 'Anxiété généralisée diagnostiquée en 2020', 'ACTIVE', 1, 25, 'LOW', NOW(), NOW()),
(2, 7, 'PAT-002', '1988-07-22', 'MALE', '14 Rue Ibn Batouta', 'Rabat', '10000', 'Maroc', 'Fatima Benjelloun', '0662-222222', 'Dépression légère, suivi depuis 6 mois', 'ACTIVE', 1, 45, 'MODERATE', NOW(), NOW()),
(3, 8, 'PAT-003', '1985-11-08', 'FEMALE', '8 Boulevard Zerktouni', 'Marrakech', '40000', 'Maroc', 'Hassan Cherkaoui', '0663-333333', 'Trouble panique avec agoraphobie', 'ON_HOLD', 2, 78, 'HIGH', NOW(), NOW()),
(4, 9, 'PAT-004', '1995-01-30', 'MALE', '33 Avenue Mohammed V', 'Fès', '30000', 'Maroc', 'Nour Fassi', '0664-444444', 'Stress post-traumatique suite accident', 'ACTIVE', 1, 15, 'LOW', NOW(), NOW()),
(5, 10, 'PAT-005', '1990-09-12', 'FEMALE', '7 Rue Allal Ben Abdellah', 'Tanger', '90000', 'Maroc', 'Khalid Berrada', '0665-555555', 'Trouble bipolaire type II', 'ACTIVE', 2, 85, 'CRITICAL', NOW(), NOW()),
(6, 11, 'PAT-006', '1993-05-25', 'MALE', '19 Avenue des FAR', 'Agadir', '80000', 'Maroc', 'Samira Lahlou', '0666-666666', 'Phobies sociales depuis adolescence', 'ACTIVE', 3, 35, 'MODERATE', NOW(), NOW()),
(7, 12, 'PAT-007', '1987-12-03', 'FEMALE', '42 Rue de la Liberté', 'Meknès', '50000', 'Maroc', 'Omar Idrissi', '0667-777777', 'TOC - compulsions de vérification', 'ACTIVE', 1, 55, 'MODERATE', NOW(), NOW()),
(8, 13, 'PAT-008', '1991-08-17', 'MALE', '56 Boulevard Anfa', 'Casablanca', '20100', 'Maroc', 'Laila Bennani', '0668-888888', 'Burnout professionnel sévère', 'ACTIVE', 2, 42, 'MODERATE', NOW(), NOW()),
(9, 14, 'PAT-009', '1989-04-28', 'FEMALE', '11 Avenue Moulay Youssef', 'Oujda', '60000', 'Maroc', 'Youssef El Amrani', '0669-999999', 'Trouble alimentaire - anorexie', 'ON_HOLD', 3, 72, 'HIGH', NOW(), NOW()),
(10, 15, 'PAT-010', '1994-06-14', 'MALE', '28 Rue Tarik Ibn Ziad', 'Tétouan', '93000', 'Maroc', 'Aicha Kettani', '0670-000000', 'Insomnie chronique depuis 2 ans', 'ACTIVE', 1, 28, 'LOW', NOW(), NOW()),
(11, 16, 'PAT-011', '1986-02-20', 'FEMALE', '15 Boulevard Mohammed VI', 'Kenitra', '14000', 'Maroc', 'Driss Squalli', '0671-111222', 'Dépression majeure récurrente', 'ACTIVE', 2, 68, 'HIGH', NOW(), NOW()),
(12, 17, 'PAT-012', '1997-10-05', 'MALE', '9 Rue des Orangers', 'Salé', '11000', 'Maroc', 'Meryem Chraibi', '0672-333444', 'Anxiété de performance - étudiant', 'ACTIVE', 1, 32, 'MODERATE', NOW(), NOW());

SELECT setval('patients_id_seq', 12);

-- ============================================
-- SEANCES
-- Types: IN_PERSON, VIDEO_CALL, PHONE_CALL, HOME_VISIT, GROUP_SESSION
-- Status: SCHEDULED, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW, RESCHEDULED
-- ============================================
INSERT INTO seances (id, seance_code, patient_id, therapeute_id, type, status, scheduled_at, duration_minutes, notes, is_recurring, reminder_sent, created_at, updated_at)
VALUES
-- Séances d'aujourd'hui
(1, 'SEA-001', 1, 1, 'IN_PERSON', 'COMPLETED', NOW() - INTERVAL '2 hours', 60, 'Bonne progression, Sara montre une amélioration significative de la gestion de son anxiété', false, true, NOW() - INTERVAL '1 week', NOW()),
(2, 'SEA-002', 2, 1, 'IN_PERSON', 'COMPLETED', NOW() - INTERVAL '1 hour', 45, 'Première consultation de suivi - Karim répond bien au traitement', false, true, NOW() - INTERVAL '3 days', NOW()),
(3, 'SEA-003', 4, 1, 'IN_PERSON', 'IN_PROGRESS', NOW(), 60, 'Séance de thérapie EMDR pour le traitement du PTSD', false, true, NOW() - INTERVAL '2 days', NOW()),
(4, 'SEA-004', 7, 1, 'IN_PERSON', 'SCHEDULED', NOW() + INTERVAL '1 hour', 45, 'Suivi mensuel TOC - évaluation des compulsions', false, false, NOW() - INTERVAL '1 day', NOW()),
(5, 'SEA-005', 10, 1, 'IN_PERSON', 'SCHEDULED', NOW() + INTERVAL '2 hours', 45, 'Évaluation du sommeil et ajustement protocole', false, false, NOW() - INTERVAL '1 day', NOW()),
(6, 'SEA-006', 3, 2, 'IN_PERSON', 'SCHEDULED', NOW() + INTERVAL '3 hours', 60, 'Gestion de la crise - agoraphobie', false, false, NOW() - INTERVAL '2 days', NOW()),
(7, 'SEA-007', 5, 2, 'VIDEO_CALL', 'SCHEDULED', NOW() + INTERVAL '4 hours', 60, 'Suivi téléconsultation - stabilisation de l''humeur', false, false, NOW() - INTERVAL '1 day', NOW()),
(8, 'SEA-008', 8, 2, 'IN_PERSON', 'SCHEDULED', NOW() + INTERVAL '5 hours', 45, 'Bilan de progression burnout', false, false, NOW() - INTERVAL '1 day', NOW()),

-- Séances passées (cette semaine)
(9, 'SEA-009', 1, 1, 'IN_PERSON', 'COMPLETED', NOW() - INTERVAL '1 day', 60, 'Exercices de respiration et relaxation progressive', false, true, NOW() - INTERVAL '1 week', NOW() - INTERVAL '1 day'),
(10, 'SEA-010', 6, 3, 'IN_PERSON', 'COMPLETED', NOW() - INTERVAL '1 day', 60, 'Thérapie d''exposition - progrès notables', false, true, NOW() - INTERVAL '1 week', NOW() - INTERVAL '1 day'),
(11, 'SEA-011', 2, 1, 'IN_PERSON', 'COMPLETED', NOW() - INTERVAL '2 days', 45, 'Ajustement du traitement antidépresseur', false, true, NOW() - INTERVAL '2 weeks', NOW() - INTERVAL '2 days'),
(12, 'SEA-012', 11, 2, 'IN_PERSON', 'COMPLETED', NOW() - INTERVAL '2 days', 60, 'Thérapie cognitive - restructuration des pensées', false, true, NOW() - INTERVAL '1 week', NOW() - INTERVAL '2 days'),
(13, 'SEA-013', 9, 3, 'IN_PERSON', 'CANCELLED', NOW() - INTERVAL '3 days', 45, 'Annulée - patiente hospitalisée', false, true, NOW() - INTERVAL '2 weeks', NOW() - INTERVAL '3 days'),
(14, 'SEA-014', 12, 1, 'IN_PERSON', 'COMPLETED', NOW() - INTERVAL '3 days', 60, 'Gestion du stress examens - techniques efficaces', false, true, NOW() - INTERVAL '1 week', NOW() - INTERVAL '3 days'),
(15, 'SEA-015', 7, 1, 'IN_PERSON', 'COMPLETED', NOW() - INTERVAL '4 days', 60, 'Techniques de relaxation et prévention rechute', false, true, NOW() - INTERVAL '2 weeks', NOW() - INTERVAL '4 days'),
(16, 'SEA-016', 5, 2, 'IN_PERSON', 'COMPLETED', NOW() - INTERVAL '5 days', 90, 'Intervention de crise - épisode hypomaniaque géré', false, true, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days');

SELECT setval('seances_id_seq', 16);

-- ============================================
-- PREDICTIONS (Alertes IA)
-- Types: NEXT_SESSION_NEEDED, TREATMENT_PROGRESS, RISK_ASSESSMENT, OPTIMAL_SCHEDULE, SESSION_OUTCOME, DROPOUT_RISK, TREATMENT_DURATION
-- risk_category: LOW, MODERATE, HIGH, CRITICAL
-- ============================================
INSERT INTO predictions (id, patient_id, type, risk_level, risk_category, confidence_score, prediction, recommendations, algorithm_used, model_version, predicted_for_date, created_at)
VALUES
(1, 3, 'DROPOUT_RISK', 82, 'CRITICAL', 0.89, 'Risque élevé d''abandon thérapeutique', 'Contacter Leila rapidement. 3 absences consécutives détectées. Proposer un appel téléphonique de suivi.', 'RandomForest', 'v2.1', NOW() + INTERVAL '7 days', NOW() - INTERVAL '2 hours'),
(2, 5, 'RISK_ASSESSMENT', 85, 'CRITICAL', 0.92, 'Score de risque en augmentation', 'Intervention urgente recommandée pour Nadia. Score anxiété en hausse significative. Envisager hospitalisation.', 'XGBoost', 'v2.1', NOW() + INTERVAL '3 days', NOW() - INTERVAL '4 hours'),
(3, 9, 'TREATMENT_PROGRESS', 72, 'HIGH', 0.78, 'Progression ralentie détectée', 'Surveiller Kenza. Tendance à la baisse du moral. Ajuster le protocole nutritionnel.', 'NeuralNetwork', 'v2.0', NOW() + INTERVAL '14 days', NOW() - INTERVAL '1 day'),
(4, 11, 'DROPOUT_RISK', 68, 'HIGH', 0.85, 'Signes de démotivation détectés', 'Hajar montre des signes de démotivation. Renforcer l''alliance thérapeutique.', 'RandomForest', 'v2.1', NOW() + INTERVAL '7 days', NOW() - INTERVAL '1 day'),
(5, 2, 'NEXT_SESSION_NEEDED', 45, 'MODERATE', 0.85, 'Séance de suivi recommandée', 'Planifier une séance pour Karim dans les 7 prochains jours. Progression positive à maintenir.', 'DecisionTree', 'v2.0', NOW() + INTERVAL '7 days', NOW() - INTERVAL '2 days'),
(6, 8, 'TREATMENT_PROGRESS', 42, 'MODERATE', 0.82, 'Amélioration légère observée', 'Rachid progresse. Continuer le protocole actuel. Envisager réduction de fréquence.', 'XGBoost', 'v2.1', NOW() + INTERVAL '30 days', NOW() - INTERVAL '3 days'),
(7, 1, 'SESSION_OUTCOME', 25, 'LOW', 0.88, 'Excellente progression thérapeutique', 'Sara montre une excellente progression. Objectifs atteints à 80%.', 'NeuralNetwork', 'v2.0', NOW() + INTERVAL '30 days', NOW() - INTERVAL '3 days'),
(8, 4, 'TREATMENT_DURATION', 15, 'LOW', 0.90, 'Fin de traitement proche', 'Mehdi proche de la fin du traitement EMDR. Préparer le plan de sortie.', 'RandomForest', 'v2.1', NOW() + INTERVAL '60 days', NOW() - INTERVAL '4 days');

SELECT setval('predictions_id_seq', 8);

-- ============================================
-- NOTIFICATIONS
-- Types: APPOINTMENT_REMINDER, APPOINTMENT_CONFIRMED, APPOINTMENT_CANCELLED, NEW_MESSAGE, TREATMENT_UPDATE, SYSTEM_ALERT, PREDICTION_ALERT, PAYMENT_RECEIVED, DOCUMENT_UPLOADED
-- Status: UNREAD, READ, ARCHIVED
-- ============================================
INSERT INTO notifications (id, user_id, title, message, type, status, is_read, is_sent, created_at)
VALUES
(1, 1, 'Alerte patient à risque', 'Leila Cherkaoui présente un risque élevé d''abandon thérapeutique. Score: 82%', 'PREDICTION_ALERT', 'UNREAD', false, true, NOW() - INTERVAL '2 hours'),
(2, 1, 'Nouvelle séance planifiée', 'Séance avec Mehdi Fassi Fihri confirmée pour demain à 14h00', 'APPOINTMENT_CONFIRMED', 'READ', true, true, NOW() - INTERVAL '1 day'),
(3, 2, 'Rappel séance', 'Séance avec Sara Ouazzani dans 1 heure - Cabinet principal', 'APPOINTMENT_REMINDER', 'UNREAD', false, true, NOW() - INTERVAL '1 hour'),
(4, 2, 'Patient prioritaire', 'Nadia Berrada nécessite une attention particulière. Score de risque: 85%', 'PREDICTION_ALERT', 'UNREAD', false, true, NOW() - INTERVAL '4 hours'),
(5, 3, 'Annulation séance', 'Kenza El Amrani a annulé sa séance du 18/12. Motif: maladie', 'APPOINTMENT_CANCELLED', 'READ', true, true, NOW() - INTERVAL '3 days'),
(6, 1, 'Mise à jour traitement', 'Le protocole de Hamza Chraibi a été mis à jour suite à la dernière évaluation', 'TREATMENT_UPDATE', 'UNREAD', false, true, NOW() - INTERVAL '5 hours');

SELECT setval('notifications_id_seq', 6);

-- ============================================
-- Afficher un résumé
-- ============================================
DO $$
DECLARE
    user_count INTEGER;
    therapeute_count INTEGER;
    patient_count INTEGER;
    seance_count INTEGER;
    prediction_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO user_count FROM users WHERE id > 1;
    SELECT COUNT(*) INTO therapeute_count FROM therapeutes;
    SELECT COUNT(*) INTO patient_count FROM patients;
    SELECT COUNT(*) INTO seance_count FROM seances;
    SELECT COUNT(*) INTO prediction_count FROM predictions;
    
    RAISE NOTICE '';
    RAISE NOTICE '╔══════════════════════════════════════════════╗';
    RAISE NOTICE '║  ✅ ClinAssist - Données insérées avec succès ║';
    RAISE NOTICE '╠══════════════════════════════════════════════╣';
    RAISE NOTICE '║  👥 % utilisateurs créés', user_count;
    RAISE NOTICE '║  👨‍⚕️ % thérapeutes', therapeute_count;
    RAISE NOTICE '║  🏥 % patients marocains', patient_count;
    RAISE NOTICE '║  📅 % séances', seance_count;
    RAISE NOTICE '║  🤖 % prédictions IA', prediction_count;
    RAISE NOTICE '╚══════════════════════════════════════════════╝';
    RAISE NOTICE '';
END $$;
