-- ClinAssist Database Initialization Script
-- This script runs automatically when PostgreSQL container starts for the first time

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Grant all privileges to clinassist user
GRANT ALL PRIVILEGES ON DATABASE clinassist TO clinassist;

-- Log initialization
DO $$
BEGIN
    RAISE NOTICE 'ClinAssist database initialized successfully!';
END $$;

