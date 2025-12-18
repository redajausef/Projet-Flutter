-- Seed seances data for ML testing
-- Creates seances with various statuses for realistic ML predictions

-- Clear existing seances if any
DELETE FROM seances;

-- Patient 1 - Sara Ouazzani (LOW risk - good attendance)
INSERT INTO seances (seance_code, patient_id, therapeute_id, scheduled_at, duration_minutes, type, status, patient_mood_before, patient_mood_after, progress_rating, created_at)
VALUES
('SEA-001', 1, 1, NOW() - INTERVAL '60 days', 45, 'IN_PERSON', 'COMPLETED', 5, 7, 4, NOW() - INTERVAL '60 days'),
('SEA-002', 1, 1, NOW() - INTERVAL '53 days', 45, 'IN_PERSON', 'COMPLETED', 6, 8, 4, NOW() - INTERVAL '53 days'),
('SEA-003', 1, 1, NOW() - INTERVAL '46 days', 45, 'IN_PERSON', 'COMPLETED', 6, 7, 3, NOW() - INTERVAL '46 days'),
('SEA-004', 1, 1, NOW() - INTERVAL '39 days', 45, 'VIDEO_CALL', 'COMPLETED', 7, 8, 4, NOW() - INTERVAL '39 days'),
('SEA-005', 1, 1, NOW() - INTERVAL '32 days', 60, 'IN_PERSON', 'COMPLETED', 7, 9, 5, NOW() - INTERVAL '32 days'),
('SEA-006', 1, 1, NOW() - INTERVAL '25 days', 45, 'IN_PERSON', 'COMPLETED', 8, 9, 5, NOW() - INTERVAL '25 days'),
('SEA-007', 1, 1, NOW() - INTERVAL '18 days', 45, 'IN_PERSON', 'COMPLETED', 7, 8, 4, NOW() - INTERVAL '18 days'),
('SEA-008', 1, 1, NOW() - INTERVAL '11 days', 45, 'VIDEO_CALL', 'COMPLETED', 8, 9, 5, NOW() - INTERVAL '11 days'),
('SEA-009', 1, 1, NOW() - INTERVAL '4 days', 45, 'IN_PERSON', 'COMPLETED', 8, 9, 5, NOW() - INTERVAL '4 days');

-- Patient 2 - Karim Benjelloun (MODERATE risk - some cancellations)
INSERT INTO seances (seance_code, patient_id, therapeute_id, scheduled_at, duration_minutes, type, status, patient_mood_before, patient_mood_after, progress_rating, created_at)
VALUES
('SEA-010', 2, 1, NOW() - INTERVAL '70 days', 45, 'IN_PERSON', 'COMPLETED', 4, 6, 3, NOW() - INTERVAL '70 days'),
('SEA-011', 2, 1, NOW() - INTERVAL '63 days', 45, 'IN_PERSON', 'CANCELLED', NULL, NULL, NULL, NOW() - INTERVAL '63 days'),
('SEA-012', 2, 1, NOW() - INTERVAL '56 days', 45, 'IN_PERSON', 'COMPLETED', 5, 6, 3, NOW() - INTERVAL '56 days'),
('SEA-013', 2, 1, NOW() - INTERVAL '49 days', 45, 'VIDEO_CALL', 'COMPLETED', 5, 7, 4, NOW() - INTERVAL '49 days'),
('SEA-014', 2, 1, NOW() - INTERVAL '42 days', 45, 'IN_PERSON', 'CANCELLED', NULL, NULL, NULL, NOW() - INTERVAL '42 days'),
('SEA-015', 2, 1, NOW() - INTERVAL '35 days', 45, 'IN_PERSON', 'COMPLETED', 5, 6, 3, NOW() - INTERVAL '35 days'),
('SEA-016', 2, 1, NOW() - INTERVAL '28 days', 45, 'IN_PERSON', 'COMPLETED', 6, 7, 4, NOW() - INTERVAL '28 days'),
('SEA-017', 2, 1, NOW() - INTERVAL '14 days', 60, 'IN_PERSON', 'COMPLETED', 5, 7, 4, NOW() - INTERVAL '14 days');

-- Patient 3 - Leila Cherkaoui (HIGH risk - multiple no-shows and cancellations)
INSERT INTO seances (seance_code, patient_id, therapeute_id, scheduled_at, duration_minutes, type, status, patient_mood_before, patient_mood_after, progress_rating, created_at)
VALUES
('SEA-018', 3, 1, NOW() - INTERVAL '90 days', 45, 'IN_PERSON', 'COMPLETED', 3, 5, 2, NOW() - INTERVAL '90 days'),
('SEA-019', 3, 1, NOW() - INTERVAL '83 days', 45, 'IN_PERSON', 'NO_SHOW', NULL, NULL, NULL, NOW() - INTERVAL '83 days'),
('SEA-020', 3, 1, NOW() - INTERVAL '76 days', 45, 'IN_PERSON', 'CANCELLED', NULL, NULL, NULL, NOW() - INTERVAL '76 days'),
('SEA-021', 3, 1, NOW() - INTERVAL '69 days', 45, 'VIDEO_CALL', 'COMPLETED', 4, 5, 2, NOW() - INTERVAL '69 days'),
('SEA-022', 3, 1, NOW() - INTERVAL '62 days', 45, 'IN_PERSON', 'NO_SHOW', NULL, NULL, NULL, NOW() - INTERVAL '62 days'),
('SEA-023', 3, 1, NOW() - INTERVAL '55 days', 45, 'IN_PERSON', 'CANCELLED', NULL, NULL, NULL, NOW() - INTERVAL '55 days'),
('SEA-024', 3, 1, NOW() - INTERVAL '48 days', 45, 'IN_PERSON', 'COMPLETED', 3, 4, 2, NOW() - INTERVAL '48 days'),
('SEA-025', 3, 1, NOW() - INTERVAL '40 days', 45, 'IN_PERSON', 'NO_SHOW', NULL, NULL, NULL, NOW() - INTERVAL '40 days');

-- Patient 4 - Mehdi Fassi (CRITICAL risk - mostly no-shows, long absence)
INSERT INTO seances (seance_code, patient_id, therapeute_id, scheduled_at, duration_minutes, type, status, patient_mood_before, patient_mood_after, progress_rating, created_at)
VALUES
('SEA-026', 4, 1, NOW() - INTERVAL '120 days', 45, 'IN_PERSON', 'COMPLETED', 2, 4, 2, NOW() - INTERVAL '120 days'),
('SEA-027', 4, 1, NOW() - INTERVAL '110 days', 45, 'IN_PERSON', 'NO_SHOW', NULL, NULL, NULL, NOW() - INTERVAL '110 days'),
('SEA-028', 4, 1, NOW() - INTERVAL '100 days', 45, 'IN_PERSON', 'NO_SHOW', NULL, NULL, NULL, NOW() - INTERVAL '100 days'),
('SEA-029', 4, 1, NOW() - INTERVAL '90 days', 45, 'VIDEO_CALL', 'CANCELLED', NULL, NULL, NULL, NOW() - INTERVAL '90 days'),
('SEA-030', 4, 1, NOW() - INTERVAL '80 days', 45, 'IN_PERSON', 'COMPLETED', 3, 4, 1, NOW() - INTERVAL '80 days'),
('SEA-031', 4, 1, NOW() - INTERVAL '70 days', 45, 'IN_PERSON', 'NO_SHOW', NULL, NULL, NULL, NOW() - INTERVAL '70 days'),
('SEA-032', 4, 1, NOW() - INTERVAL '60 days', 45, 'IN_PERSON', 'CANCELLED', NULL, NULL, NULL, NOW() - INTERVAL '60 days');
