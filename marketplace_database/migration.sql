-- ================================================================
-- MARKETPLACE PRO — Database Migration
-- Run this on each respective database node
-- ================================================================

-- ============================================
-- NODE 1: marketplace_node1
-- ============================================
\c marketplace_node1;

-- Update user roles: only USER, EXTERNAL_STORE, ADMIN
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('USER', 'EXTERNAL_STORE', 'ADMIN'));

-- Add avatar_url column to users
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(500);

-- Create cart_items table
CREATE TABLE IF NOT EXISTS cart_items (
    cart_item_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1 CHECK (quantity > 0),
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT uq_cart_user_product UNIQUE (user_id, product_id)
);

CREATE INDEX IF NOT EXISTS idx_cart_user ON cart_items(user_id);

-- ============================================
-- NODE 2: marketplace_node2
-- ============================================
\c marketplace_node2;

-- Add image_url column to products
ALTER TABLE products ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);

-- ============================================
-- NODE 3: marketplace_node3
-- (No changes needed for node 3)
-- ============================================
