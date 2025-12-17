-- ClinAssist - Données simplifiées avec mots de passe en clair
-- Pour projet académique

DELETE FROM seances WHERE id > 0;
DELETE FROM patients WHERE id > 0;
DELETE FROM therapeutes WHERE id > 0;
DELETE FROM users WHERE id > 0;

-- UTILISATEURS (mots de passe en clair pour NoOpPasswordEncoder)
INSERT INTO users (id, username, email, password, first_name, last_name, phone_number, role, is_active, is_email_verified, created_at, updated_at)
VALUES
(1, 'dr.martin', 'martin@clinassist.com', 'password123', 'Sophie', 'Martin', '0661-234567', 'THERAPEUTE', true, true, NOW(), NOW()),
(2, 'sara.ouazzani', 'sara.ouazzani@gmail.com', 'password123', 'Sara', 'Ouazzani', '0665-123456', 'PATIENT', true, true, NOW(), NOW()),
(3, 'karim.benjelloun', 'karim.benjelloun@gmail.com', 'password123', 'Karim', 'Benjelloun', '0666-234567', 'PATIENT', true, true, NOW(), NOW()),
(4, 'leila.cherkaoui', 'leila.cherkaoui@gmail.com', 'password123', 'Leila', 'Cherkaoui', '0667-345678', 'PATIENT', true, true, NOW(), NOW()),
(5, 'mehdi.fassi', 'mehdi.fassi@gmail.com', 'password123', 'Mehdi', 'Fassi', '0668-456789', 'PATIENT', true, true, NOW(), NOW());
SELECT setval('users_id_seq', 5);

-- THERAPEUTE
INSERT INTO therapeutes (id, user_id, therapeute_code, specialization, license_number, years_of_experience, biography, status, rating, total_reviews, consultation_fee, currency, created_at, updated_at)
VALUES (1, 1, 'THER-001', 'Psychologie clinique', 'PSY-2015-001', 9, 'Spécialiste TCC', 'AVAILABLE', 4.8, 45, 500.00, 'MAD', NOW(), NOW());
SELECT setval('therapeutes_id_seq', 1);

-- PATIENTS
INSERT INTO patients (id, user_id, patient_code, date_of_birth, gender, address, city, postal_code, country, status, assigned_therapeute_id, risk_score, risk_category, created_at, updated_at)
VALUES
(1, 2, 'PAT-001', '1992-03-15', 'FEMALE', '25 Avenue Hassan II', 'Casablanca', '20000', 'Maroc', 'ACTIVE', 1, 25, 'LOW', NOW(), NOW()),
(2, 3, 'PAT-002', '1988-07-22', 'MALE', '14 Rue Ibn Batouta', 'Rabat', '10000', 'Maroc', 'ACTIVE', 1, 45, 'MODERATE', NOW(), NOW()),
(3, 4, 'PAT-003', '1985-11-08', 'FEMALE', '8 Boulevard Zerktouni', 'Marrakech', '40000', 'Maroc', 'ACTIVE', 1, 78, 'HIGH', NOW(), NOW()),
(4, 5, 'PAT-004', '1995-01-30', 'MALE', '33 Avenue Mohammed V', 'Fès', '30000', 'Maroc', 'ACTIVE', 1, 15, 'LOW', NOW(), NOW());
SELECT setval('patients_id_seq', 4);

-- SEANCES
INSERT INTO seances (id, seance_code, patient_id, therapeute_id, type, status, scheduled_at, duration_minutes, notes, is_recurring, reminder_sent, created_at, updated_at)
VALUES
(1, 'SEA-001', 1, 1, 'IN_PERSON', 'COMPLETED', NOW() - INTERVAL '2 hours', 60, 'Bonne progression', false, true, NOW() - INTERVAL '1 week', NOW()),
(2, 'SEA-002', 2, 1, 'IN_PERSON', 'COMPLETED', NOW() - INTERVAL '1 hour', 45, 'Suivi positif', false, true, NOW() - INTERVAL '3 days', NOW()),
(3, 'SEA-003', 3, 1, 'IN_PERSON', 'SCHEDULED', NOW() + INTERVAL '1 hour', 60, 'Séance planifiée', false, false, NOW() - INTERVAL '2 days', NOW()),
(4, 'SEA-004', 4, 1, 'VIDEO_CALL', 'SCHEDULED', NOW() + INTERVAL '3 hours', 45, 'Téléconsultation', false, false, NOW() - INTERVAL '1 day', NOW()),
(5, 'SEA-005', 1, 1, 'IN_PERSON', 'COMPLETED', NOW() - INTERVAL '1 day', 60, 'Exercices', false, true, NOW() - INTERVAL '1 week', NOW()),
(6, 'SEA-006', 2, 1, 'IN_PERSON', 'COMPLETED', NOW() - INTERVAL '2 days', 45, 'Thérapie', false, true, NOW() - INTERVAL '2 weeks', NOW()),
(7, 'SEA-007', 3, 1, 'IN_PERSON', 'COMPLETED', NOW() - INTERVAL '3 days', 60, 'Gestion stress', false, true, NOW() - INTERVAL '1 week', NOW()),
(8, 'SEA-008', 4, 1, 'IN_PERSON', 'COMPLETED', NOW() - INTERVAL '4 days', 45, 'Suivi', false, true, NOW() - INTERVAL '2 weeks', NOW());
SELECT setval('seances_id_seq', 8);

DO $$ BEGIN RAISE NOTICE 'Data créée: 1 thérapeute, 4 patients, 8 séances'; END $$;
