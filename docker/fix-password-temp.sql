-- Fix password to plain text for academic project
UPDATE users SET password = 'password123' WHERE role IN ('THERAPEUTE', 'PATIENT');
UPDATE users SET password = 'admin123' WHERE role = 'ADMIN';
