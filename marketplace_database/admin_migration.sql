-- ============================================================
--  ADMIN MIGRATION SCRIPT
--  Run this on an existing database to add admin system support
-- ============================================================

\c marketplace_node1;

-- Add is_active column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'users' AND column_name = 'is_active'
    ) THEN
        ALTER TABLE users ADD COLUMN is_active BOOLEAN DEFAULT TRUE;
        UPDATE users SET is_active = TRUE;
    END IF;
END $$;

-- Update admin user with a known loginable password
-- Password: admin12345, Salt: m3n4o5p6q7r8s9t0
-- Hash computed via SHA-256(salt + password) -> Base64
UPDATE users
SET password_hash = 'TeEf8lN2puv+4chyMfHZbhz87oc9JSNvbM8loeAzctw=',
    salt = 'm3n4o5p6q7r8s9t0',
    is_active = TRUE
WHERE user_id = 13 AND role = 'ADMIN';

-- If no admin exists, create one
INSERT INTO users (username, email, password_hash, salt, role, is_active, is_verified)
SELECT 'admin_main', 'admin@marketplace.com',
       'TeEf8lN2puv+4chyMfHZbhz87oc9JSNvbM8loeAzctw=',
       'm3n4o5p6q7r8s9t0', 'ADMIN', TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE role = 'ADMIN');
