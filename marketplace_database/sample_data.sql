-- ============================================================
--  MARKETPLACE SAMPLE DATA
--  Covers: marketplace_node1, marketplace_node2, marketplace_node3
-- ============================================================

-- ============================================================
--  NODE 1  –  Users, Accounts, Chats, Cart
-- ============================================================
\c marketplace_node1;

-- ── unique_emails ─────────────────────────────────────────
INSERT INTO unique_emails (email, user_id) VALUES
  ('ahmed.hassan@gmail.com',       1),
  ('sara.ibrahim@yahoo.com',       2),
  ('mohamed.ali@hotmail.com',      3),
  ('nour.khalil@gmail.com',        4),
  ('kareem.mansour@outlook.com',   5),
  ('layla.farouk@gmail.com',       6),
  ('omar.samir@yahoo.com',         7),
  ('dina.youssef@gmail.com',       8),
  ('tarek.naguib@hotmail.com',     9),
  ('hana.rezk@gmail.com',         10),
  ('youssef.store@techzone.com',  11),
  ('menna.boutique@fashion.eg',   12),
  ('admin@marketplace.com',       13),
  ('ramy.electronics@shop.eg',    14),
  ('salma.crafts@etsy.eg',        15);

-- ── users ─────────────────────────────────────────────────
-- Roles: USER (1-10), EXTERNAL_STORE (11,12,14,15), ADMIN (13)
INSERT INTO users (user_id, username, email, password_hash, salt, role, avatar_url, is_verified, created_at, updated_at) VALUES
  ( 1, 'ahmed_hassan',   'ahmed.hassan@gmail.com',      '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMqJqhcanFp8gL9ZiHs1RzFi7e', 'a1b2c3d4e5f6g7h8', 'USER',           'https://cdn.marketplace.com/avatars/1.jpg',  TRUE,  '2025-01-05 09:00:00', '2025-03-10 14:22:00'),
  ( 2, 'sara_ibrahim',   'sara.ibrahim@yahoo.com',       '$2b$12$Km8nPqRsT1uVwXyZ2AbCdEfGhIjKlMnOpQrStUvWxYz3456789012', 'b2c3d4e5f6g7h8i9', 'USER',           'https://cdn.marketplace.com/avatars/2.jpg',  TRUE,  '2025-01-10 11:30:00', '2025-04-01 08:15:00'),
  ( 3, 'mo_ali',         'mohamed.ali@hotmail.com',      '$2b$12$XyZ1AbCdEfGhIjKlMnOpQrStUvWxYz34567890123456789012345', 'c3d4e5f6g7h8i9j0', 'USER',           NULL,                                         FALSE, '2025-01-15 16:45:00', '2025-01-15 16:45:00'),
  ( 4, 'nour_khalil',    'nour.khalil@gmail.com',        '$2b$12$AbCdEfGhIjKlMnOpQrStUvWxYz3456789012345678901234567890', 'd4e5f6g7h8i9j0k1', 'USER',           'https://cdn.marketplace.com/avatars/4.jpg',  TRUE,  '2025-02-01 10:00:00', '2025-05-10 09:30:00'),
  ( 5, 'kareem_m',       'kareem.mansour@outlook.com',   '$2b$12$BcDeFgHiJkLmNoPqRsTuVwXyZ12345678901234567890123456789', 'e5f6g7h8i9j0k1l2', 'USER',           'https://cdn.marketplace.com/avatars/5.jpg',  TRUE,  '2025-02-14 13:20:00', '2025-06-01 11:00:00'),
  ( 6, 'layla_farouk',   'layla.farouk@gmail.com',       '$2b$12$CdEfGhIjKlMnOpQrStUvWxYz123456789012345678901234567890', 'f6g7h8i9j0k1l2m3', 'USER',           'https://cdn.marketplace.com/avatars/6.jpg',  TRUE,  '2025-02-20 08:00:00', '2025-04-15 17:45:00'),
  ( 7, 'omar_samir',     'omar.samir@yahoo.com',         '$2b$12$DeFgHiJkLmNoPqRsTuVwXyZ1234567890123456789012345678901', 'g7h8i9j0k1l2m3n4', 'USER',           NULL,                                         FALSE, '2025-03-01 12:00:00', '2025-03-01 12:00:00'),
  ( 8, 'dina_youssef',   'dina.youssef@gmail.com',       '$2b$12$EfGhIjKlMnOpQrStUvWxYz12345678901234567890123456789012', 'h8i9j0k1l2m3n4o5', 'USER',           'https://cdn.marketplace.com/avatars/8.jpg',  TRUE,  '2025-03-05 15:30:00', '2025-06-10 10:00:00'),
  ( 9, 'tarek_naguib',   'tarek.naguib@hotmail.com',     '$2b$12$FgHiJkLmNoPqRsTuVwXyZ123456789012345678901234567890123', 'i9j0k1l2m3n4o5p6', 'USER',           'https://cdn.marketplace.com/avatars/9.jpg',  TRUE,  '2025-03-12 07:45:00', '2025-05-20 13:30:00'),
  (10, 'hana_rezk',      'hana.rezk@gmail.com',          '$2b$12$GhIjKlMnOpQrStUvWxYz1234567890123456789012345678901234', 'j0k1l2m3n4o5p6q7', 'USER',           'https://cdn.marketplace.com/avatars/10.jpg', TRUE,  '2025-03-18 09:15:00', '2025-06-05 16:20:00'),
  (11, 'techzone_store', 'youssef.store@techzone.com',   '$2b$12$HiJkLmNoPqRsTuVwXyZ12345678901234567890123456789012345', 'k1l2m3n4o5p6q7r8', 'EXTERNAL_STORE', 'https://cdn.marketplace.com/avatars/11.jpg', TRUE,  '2024-11-01 10:00:00', '2025-06-01 09:00:00'),
  (12, 'menna_boutique', 'menna.boutique@fashion.eg',    '$2b$12$IjKlMnOpQrStUvWxYz123456789012345678901234567890123456', 'l2m3n4o5p6q7r8s9', 'EXTERNAL_STORE', 'https://cdn.marketplace.com/avatars/12.jpg', TRUE,  '2024-11-15 14:00:00', '2025-05-15 11:30:00'),
  (13, 'admin_main',     'admin@marketplace.com',        '$2b$12$JkLmNoPqRsTuVwXyZ1234567890123456789012345678901234567', 'm3n4o5p6q7r8s9t0', 'ADMIN',          'https://cdn.marketplace.com/avatars/13.jpg', TRUE,  '2024-10-01 08:00:00', '2025-06-01 08:00:00'),
  (14, 'ramy_electronics','ramy.electronics@shop.eg',    '$2b$12$KlMnOpQrStUvWxYz12345678901234567890123456789012345678', 'n4o5p6q7r8s9t0u1', 'EXTERNAL_STORE', 'https://cdn.marketplace.com/avatars/14.jpg', TRUE,  '2024-12-01 10:00:00', '2025-05-01 10:00:00'),
  (15, 'salma_crafts',   'salma.crafts@etsy.eg',         '$2b$12$LmNoPqRsTuVwXyZ123456789012345678901234567890123456789', 'o5p6q7r8s9t0u1v2', 'EXTERNAL_STORE', 'https://cdn.marketplace.com/avatars/15.jpg', TRUE,  '2025-01-20 11:00:00', '2025-04-10 14:00:00');

-- ── accounts ──────────────────────────────────────────────
INSERT INTO accounts (account_id, user_id, balance, currency, updated_at) VALUES
  ( 1,  1,  2500.00, 'EGP', '2026-05-01 10:00:00'),
  ( 2,  2,  1800.50, 'EGP', '2026-04-28 14:30:00'),
  ( 3,  3,   350.00, 'EGP', '2026-03-15 09:00:00'),
  ( 4,  4,  5000.00, 'EGP', '2026-05-10 11:20:00'),
  ( 5,  5,   750.75, 'EGP', '2026-05-05 16:00:00'),
  ( 6,  6,  3200.00, 'EGP', '2026-04-20 08:45:00'),
  ( 7,  7,     0.00, 'EGP', '2026-03-01 12:00:00'),
  ( 8,  8,  1100.25, 'EGP', '2026-05-08 10:30:00'),
  ( 9,  9,  4300.00, 'EGP', '2026-05-02 13:15:00'),
  (10, 10,   920.00, 'EGP', '2026-04-25 17:00:00'),
  (11, 11, 48500.00, 'EGP', '2026-05-11 09:00:00'),
  (12, 12, 22000.00, 'EGP', '2026-05-11 09:05:00'),
  (13, 13,     0.00, 'EGP', '2026-01-01 00:00:00'),
  (14, 14, 35750.00, 'EGP', '2026-05-10 10:00:00'),
  (15, 15,  9800.00, 'EGP', '2026-05-09 15:30:00');

-- ── chat_messages ─────────────────────────────────────────
INSERT INTO chat_messages (message_id, sender_id, receiver_id, content, is_read, timestamp) VALUES
  ( 1,  1, 11, 'هل المنتج متاح للشحن لمحافظة الجيزة؟',                       TRUE,  '2026-04-10 09:15:00'),
  ( 2, 11,  1, 'نعم، يتم الشحن لجميع المحافظات خلال 3-5 أيام عمل.',           TRUE,  '2026-04-10 09:45:00'),
  ( 3,  1, 11, 'ممتاز! هل يوجد خصم على الكميات الكبيرة؟',                    TRUE,  '2026-04-10 10:00:00'),
  ( 4, 11,  1, 'نعم، عند شراء 3 قطع أو أكثر تحصل على خصم 10%.',              TRUE,  '2026-04-10 10:20:00'),
  ( 5,  2, 12, 'What sizes are available for the summer dress?',               TRUE,  '2026-04-15 14:00:00'),
  ( 6, 12,  2, 'We have sizes S, M, L, and XL available in all colors.',       TRUE,  '2026-04-15 14:30:00'),
  ( 7,  2, 12, 'Can I exchange if the size doesn''t fit?',                     TRUE,  '2026-04-15 14:45:00'),
  ( 8, 12,  2, 'Of course! Free exchange within 14 days of delivery.',         FALSE, '2026-04-15 15:00:00'),
  ( 9,  4, 14, 'Is the Samsung Galaxy S25 in stock?',                          TRUE,  '2026-05-01 11:00:00'),
  (10, 14,  4, 'Yes! We have the 256GB and 512GB variants.',                   TRUE,  '2026-05-01 11:15:00'),
  (11,  4, 14, 'Does it come with a local warranty?',                          TRUE,  '2026-05-01 11:20:00'),
  (12, 14,  4, 'Yes, 2-year local warranty and free screen protector.',        FALSE, '2026-05-01 11:35:00'),
  (13,  5,  8, 'Hi! Did you receive the item you ordered last week?',          TRUE,  '2026-04-22 16:00:00'),
  (14,  8,  5, 'Yes, arrived in perfect condition. Thank you!',                TRUE,  '2026-04-22 16:30:00'),
  (15,  6, 15, 'I love your handmade bags! Can you do custom orders?',         TRUE,  '2026-05-03 10:00:00'),
  (16, 15,  6, 'Absolutely! Custom orders take 7-10 days. DM me your design.', FALSE, '2026-05-03 10:45:00'),
  (17,  9, 11, 'What is the warranty period on the laptop?',                   TRUE,  '2026-05-05 13:00:00'),
  (18, 11,  9, '1-year international warranty + 6-month local service.',       TRUE,  '2026-05-05 13:20:00'),
  (19, 10, 12, 'Do you ship internationally?',                                 FALSE, '2026-05-10 09:00:00'),
  (20,  3, 14, 'هل يوجد كفالة على التلفزيون؟',                                FALSE, '2026-05-11 08:30:00');

-- ── cart_items ────────────────────────────────────────────
INSERT INTO cart_items (cart_item_id, user_id, product_id, quantity, added_at) VALUES
  ( 1,  1,  3, 1, '2026-05-10 10:00:00'),
  ( 2,  1,  7, 2, '2026-05-10 10:05:00'),
  ( 3,  2,  1, 1, '2026-05-09 14:30:00'),
  ( 4,  2, 12, 3, '2026-05-09 14:35:00'),
  ( 5,  4,  5, 1, '2026-05-08 11:00:00'),
  ( 6,  4, 15, 1, '2026-05-08 11:10:00'),
  ( 7,  5,  2, 1, '2026-05-07 16:20:00'),
  ( 8,  6,  8, 1, '2026-05-10 09:15:00'),
  ( 9,  6, 11, 2, '2026-05-10 09:20:00'),
  (10,  8,  4, 1, '2026-05-06 12:00:00'),
  (11,  9,  6, 1, '2026-05-11 08:00:00'),
  (12, 10, 10, 1, '2026-05-09 17:00:00'),
  (13, 10, 14, 1, '2026-05-09 17:05:00'),
  (14,  3,  9, 1, '2026-05-11 07:30:00'),
  (15,  7, 13, 1, '2026-04-30 20:00:00');

SELECT setval(pg_get_serial_sequence('users', 'user_id'), COALESCE(MAX(user_id), 1)) FROM users;
SELECT setval(pg_get_serial_sequence('accounts', 'account_id'), COALESCE(MAX(account_id), 1)) FROM accounts;
SELECT setval(pg_get_serial_sequence('chat_messages', 'message_id'), COALESCE(MAX(message_id), 1)) FROM chat_messages;
SELECT setval(pg_get_serial_sequence('cart_items', 'cart_item_id'), COALESCE(MAX(cart_item_id), 1)) FROM cart_items;

-- ============================================================
--  NODE 2  –  Categories, Products, Inventory
-- ============================================================
\c marketplace_node2;

-- ── categories ────────────────────────────────────────────
INSERT INTO categories (category_id, name, description) VALUES
  ( 1, 'Electronics',        'Smartphones, laptops, tablets, and all electronic gadgets'),
  ( 2, 'Fashion & Clothing', 'Men, women, and children clothing, shoes, and accessories'),
  ( 3, 'Home & Furniture',   'Furniture, home décor, kitchen appliances, and bedding'),
  ( 4, 'Books & Education',  'Textbooks, novels, stationery, and educational materials'),
  ( 5, 'Sports & Outdoors',  'Fitness equipment, outdoor gear, and sports accessories'),
  ( 6, 'Beauty & Personal Care', 'Skincare, makeup, hair care, and personal hygiene products'),
  ( 7, 'Handmade & Crafts',  'Unique handcrafted items, artwork, and DIY supplies'),
  ( 8, 'Automotive',         'Car accessories, spare parts, and tools'),
  ( 9, 'Toys & Kids',        'Toys, games, and products for children'),
  (10, 'Food & Grocery',     'Packaged food, spices, organic products, and beverages');

-- ── products ──────────────────────────────────────────────
INSERT INTO products (product_id, seller_id, category_id, name, brand, description, price, image_url, status, created_at, updated_at) VALUES
  -- Electronics (seller: TechZone #11, Ramy Electronics #14)
  ( 1, 11, 1, 'Samsung Galaxy S25 Ultra 512GB',           'Samsung',  'Flagship smartphone with 200MP camera, S-Pen, 5000mAh battery, AI features.', 45999.00, 'https://cdn.marketplace.com/products/1.jpg',  'AVAILABLE', '2025-02-01 10:00:00', '2026-04-15 12:00:00'),
  ( 2, 11, 1, 'MacBook Pro M4 14-inch 16GB/512GB',        'Apple',    'Professional laptop with M4 chip, Liquid Retina XDR display, 18-hr battery.',  89999.00, 'https://cdn.marketplace.com/products/2.jpg',  'AVAILABLE', '2025-02-05 11:00:00', '2026-05-01 09:00:00'),
  ( 3, 14, 1, 'Sony PlayStation 5 Slim + Extra Controller','Sony',    'PS5 Slim console bundle with DualSense controller and 1TB SSD.',               24999.00, 'https://cdn.marketplace.com/products/3.jpg',  'AVAILABLE', '2025-03-01 09:00:00', '2026-04-20 10:00:00'),
  ( 4, 11, 1, 'Apple iPad Air M2 256GB WiFi',             'Apple',    '11-inch iPad Air with M2 chip, all-day battery, Apple Pencil compatible.',     28500.00, 'https://cdn.marketplace.com/products/4.jpg',  'AVAILABLE', '2025-03-10 14:00:00', '2026-03-10 14:00:00'),
  ( 5, 14, 1, 'Samsung 65" QLED 4K Smart TV 2025',        'Samsung',  '65-inch QLED TV with Neo Quantum Processor 4K, Tizen OS, 120Hz.',             32000.00, 'https://cdn.marketplace.com/products/5.jpg',  'AVAILABLE', '2025-04-01 10:00:00', '2026-04-01 10:00:00'),
  ( 6, 14, 1, 'Sony WH-1000XM6 Wireless Headphones',      'Sony',     'Industry-leading noise cancelling headphones, 30hr battery, multipoint BT.',   9800.00, 'https://cdn.marketplace.com/products/6.jpg',  'AVAILABLE', '2025-04-15 11:00:00', '2026-05-01 11:00:00'),
  ( 7, 11, 1, 'Lenovo ThinkPad X1 Carbon Gen 12',         'Lenovo',   'Ultralight business laptop, Intel Core Ultra 7, 32GB RAM, 1TB SSD.',           72000.00, 'https://cdn.marketplace.com/products/7.jpg',  'AVAILABLE', '2025-05-01 09:00:00', '2026-04-10 09:00:00'),

  -- Fashion (seller: Menna Boutique #12)
  ( 8, 12, 2, 'Linen Summer Maxi Dress - Floral Print',   'Menna''s', 'Breathable 100% linen dress, available in S/M/L/XL, 5 color options.',           650.00, 'https://cdn.marketplace.com/products/8.jpg',  'AVAILABLE', '2025-03-20 10:00:00', '2026-04-05 10:00:00'),
  ( 9, 12, 2, 'Men''s Slim-Fit Chino Pants',              'Menna''s', 'Stretch cotton chino trousers, 6 colors, waist sizes 28-40.',                     420.00, 'https://cdn.marketplace.com/products/9.jpg',  'AVAILABLE', '2025-04-01 11:00:00', '2026-04-01 11:00:00'),
  (10, 12, 2, 'Leather Ankle Boots - Women',              'Menna''s', 'Genuine leather ankle boots, cushioned insole, sizes 36-42.',                    1200.00, 'https://cdn.marketplace.com/products/10.jpg', 'AVAILABLE', '2025-04-10 14:00:00', '2026-03-15 14:00:00'),
  (11, 12, 2, 'Classic Denim Jacket - Unisex',            'Menna''s', '100% cotton denim jacket, washed finish, available in 3 shades.',                 780.00, 'https://cdn.marketplace.com/products/11.jpg', 'AVAILABLE', '2025-05-01 09:00:00', '2026-04-20 09:00:00'),

  -- Handmade & Crafts (seller: Salma Crafts #15)
  (12, 15, 7, 'Handmade Leather Tote Bag - Caramel',      'Salma''s Crafts', 'Hand-stitched genuine leather tote, reinforced handles, interior pocket.',  2200.00, 'https://cdn.marketplace.com/products/12.jpg', 'AVAILABLE', '2025-04-05 10:00:00', '2026-05-01 10:00:00'),
  (13, 15, 7, 'Macramé Wall Hanging - Boho 80cm',         'Salma''s Crafts', 'Hand-knotted cotton macramé art, 80cm wide, natural or dyed variants.',      850.00, 'https://cdn.marketplace.com/products/13.jpg', 'AVAILABLE', '2025-04-20 11:00:00', '2026-04-20 11:00:00'),
  (14, 15, 7, 'Hand-Painted Ceramic Mug Set (4 pcs)',     'Salma''s Crafts', 'Set of 4 hand-painted ceramic mugs, each unique, dishwasher safe.',           600.00, 'https://cdn.marketplace.com/products/14.jpg', 'AVAILABLE', '2025-05-05 09:00:00', '2026-05-05 09:00:00'),

  -- Home & Furniture
  (15, 11, 3, 'Xiaomi Smart Air Purifier 4 Pro',          'Xiaomi',  'HEPA H13 filter, covers 60m², PM2.5 sensor, app control, ultra-quiet.',           4500.00, 'https://cdn.marketplace.com/products/15.jpg', 'AVAILABLE', '2025-03-15 10:00:00', '2026-05-01 10:00:00'),
  (16, 14, 3, 'Philips Airfryer XXL 7.3L',                'Philips', 'Rapid Air technology, 7.3L capacity, 6 cooking presets, fat removal.',            7800.00, 'https://cdn.marketplace.com/products/16.jpg', 'AVAILABLE', '2025-04-01 09:00:00', '2026-04-01 09:00:00'),

  -- Already sold / removed
  (17, 11, 1, 'iPhone 15 Pro Max 256GB Natural Titanium',  'Apple',  'Previous-gen flagship, sold as part of bundle promotion.',                        52000.00, 'https://cdn.marketplace.com/products/17.jpg', 'SOLD',      '2025-01-10 10:00:00', '2026-03-01 12:00:00'),
  (18, 12, 2, 'Overstock Winter Coat Bundle (3 pcs)',      'Menna''s','Bundle listing removed after stock clearance.',                                    3200.00, 'https://cdn.marketplace.com/products/18.jpg', 'REMOVED',   '2025-02-01 10:00:00', '2026-02-15 09:00:00');

-- ── inventory ─────────────────────────────────────────────
INSERT INTO inventory (inventory_id, product_id, quantity, warehouse_node, updated_at) VALUES
  ( 1,  1,  45, 'node2_cairo',      '2026-04-15 12:00:00'),
  ( 2,  2,  12, 'node2_cairo',      '2026-05-01 09:00:00'),
  ( 3,  3,  28, 'node2_cairo',      '2026-04-20 10:00:00'),
  ( 4,  4,  30, 'node2_alex',       '2026-03-10 14:00:00'),
  ( 5,  5,   8, 'node2_cairo',      '2026-04-01 10:00:00'),
  ( 6,  6,  60, 'node2_cairo',      '2026-05-01 11:00:00'),
  ( 7,  7,  15, 'node2_alex',       '2026-04-10 09:00:00'),
  ( 8,  8, 120, 'node2_cairo',      '2026-04-05 10:00:00'),
  ( 9,  9,  85, 'node2_cairo',      '2026-04-01 11:00:00'),
  (10, 10,  40, 'node2_cairo',      '2026-03-15 14:00:00'),
  (11, 11, 200, 'node2_alex',       '2026-04-20 09:00:00'),
  (12, 12,  22, 'node2_cairo',      '2026-05-01 10:00:00'),
  (13, 13,  35, 'node2_cairo',      '2026-04-20 11:00:00'),
  (14, 14,  50, 'node2_cairo',      '2026-05-05 09:00:00'),
  (15, 15,  18, 'node2_cairo',      '2026-05-01 10:00:00'),
  (16, 16,  25, 'node2_alex',       '2026-04-01 09:00:00'),
  (17, 17,   0, 'node2_cairo',      '2026-03-01 12:00:00'),
  (18, 18,   0, 'node2_cairo',      '2026-02-15 09:00:00');

SELECT setval(pg_get_serial_sequence('categories', 'category_id'), COALESCE(MAX(category_id), 1)) FROM categories;
SELECT setval(pg_get_serial_sequence('products', 'product_id'), COALESCE(MAX(product_id), 1)) FROM products;
SELECT setval(pg_get_serial_sequence('inventory', 'inventory_id'), COALESCE(MAX(inventory_id), 1)) FROM inventory;


-- ============================================================
--  NODE 3  –  Transactions, Reports
-- ============================================================
\c marketplace_node3;

-- ── transactions ──────────────────────────────────────────
-- Q1 2026 transactions
INSERT INTO transactions (transaction_id, buyer_id, seller_id, product_id, quantity, amount, status, type, created_at, completed_at) VALUES
  ( 1,  1, 11,  1, 1, 45999.00, 'COMPLETED', 'PURCHASE',   '2026-01-08 10:30:00', '2026-01-10 14:00:00'),
  ( 2,  2, 12,  8, 2,  1300.00, 'COMPLETED', 'PURCHASE',   '2026-01-15 14:00:00', '2026-01-17 10:30:00'),
  ( 3,  4,  1, NULL, NULL, 5000.00, 'COMPLETED', 'DEPOSIT', '2026-01-20 09:00:00', '2026-01-20 09:01:00'),
  ( 4,  5, 14,  3, 1, 24999.00, 'COMPLETED', 'PURCHASE',   '2026-01-25 11:00:00', '2026-01-27 15:00:00'),
  ( 5,  6,  1, NULL, NULL, 3000.00, 'COMPLETED', 'DEPOSIT', '2026-02-01 08:00:00', '2026-02-01 08:01:00'),
  ( 6,  8, 15, 12, 1,  2200.00, 'COMPLETED', 'PURCHASE',   '2026-02-05 13:00:00', '2026-02-08 11:00:00'),
  ( 7,  9, 11,  7, 1, 72000.00, 'COMPLETED', 'PURCHASE',   '2026-02-10 10:00:00', '2026-02-12 14:30:00'),
  ( 8, 10, 12, 10, 1,  1200.00, 'COMPLETED', 'PURCHASE',   '2026-02-18 16:00:00', '2026-02-20 12:00:00'),
  ( 9,  1,  1, NULL, NULL, 2000.00, 'COMPLETED', 'DEPOSIT', '2026-02-22 09:00:00', '2026-02-22 09:01:00'),
  (10,  3, 14,  5, 1, 32000.00, 'FAILED',    'PURCHASE',   '2026-02-28 12:00:00', NULL),
  (11,  2, 15, 13, 2,  1700.00, 'COMPLETED', 'PURCHASE',   '2026-03-05 11:00:00', '2026-03-08 10:00:00'),
  (12,  4, 11,  4, 1, 28500.00, 'COMPLETED', 'PURCHASE',   '2026-03-10 14:00:00', '2026-03-12 16:00:00'),
  (13,  6, 12,  8, 1,   650.00, 'COMPLETED', 'PURCHASE',   '2026-03-15 09:30:00', '2026-03-17 11:00:00'),
  (14,  5,  1, NULL, NULL, 1000.00, 'COMPLETED', 'WITHDRAWAL','2026-03-18 10:00:00','2026-03-18 10:05:00'),
  (15,  7, 14,  6, 1,  9800.00, 'PENDING',   'PURCHASE',   '2026-03-28 20:00:00', NULL);

-- Q2 2026 transactions
INSERT INTO transactions (transaction_id, buyer_id, seller_id, product_id, quantity, amount, status, type, created_at, completed_at) VALUES
  (16,  1, 11,  2, 1, 89999.00, 'COMPLETED', 'PURCHASE',   '2026-04-02 09:00:00', '2026-04-05 13:00:00'),
  (17,  8, 15, 14, 1,   600.00, 'COMPLETED', 'PURCHASE',   '2026-04-06 12:00:00', '2026-04-09 10:00:00'),
  (18,  9,  1, NULL, NULL, 4000.00, 'COMPLETED', 'DEPOSIT', '2026-04-08 08:30:00', '2026-04-08 08:31:00'),
  (19,  2, 12, 11, 1,   780.00, 'COMPLETED', 'PURCHASE',   '2026-04-12 15:00:00', '2026-04-14 11:00:00'),
  (20,  4, 11,  6, 1,  9800.00, 'COMPLETED', 'PURCHASE',   '2026-04-18 11:00:00', '2026-04-20 14:00:00'),
  (21,  6, 14, 16, 1,  7800.00, 'COMPLETED', 'PURCHASE',   '2026-04-22 10:00:00', '2026-04-25 12:00:00'),
  (22, 10, 15, 12, 1,  2200.00, 'COMPLETED', 'PURCHASE',   '2026-04-28 17:00:00', '2026-05-01 10:00:00'),
  (23,  5,  1, NULL, NULL, 2000.00, 'COMPLETED', 'DEPOSIT', '2026-05-01 16:00:00', '2026-05-01 16:01:00'),
  (24,  3, 11,  3, 1, 24999.00, 'REFUNDED',  'PURCHASE',   '2026-05-03 13:00:00', '2026-05-05 09:00:00'),
  (25,  8, 12,  9, 2,   840.00, 'COMPLETED', 'PURCHASE',   '2026-05-06 10:00:00', '2026-05-08 14:00:00'),
  (26,  1,  1, NULL, NULL, 500.00, 'COMPLETED', 'WITHDRAWAL','2026-05-07 11:00:00','2026-05-07 11:05:00'),
  (27,  9, 14,  5, 1, 32000.00, 'PENDING',   'PURCHASE',   '2026-05-11 08:00:00', NULL),
  (28, 10, 11,  1, 1, 45999.00, 'PENDING',   'PURCHASE',   '2026-05-11 08:45:00', NULL),
  (29,  6, 15, 14, 3,  1800.00, 'COMPLETED', 'PURCHASE',   '2026-05-09 14:00:00', '2026-05-11 10:00:00'),
  (30,  4,  1, NULL, NULL, 1000.00, 'COMPLETED', 'WITHDRAWAL','2026-05-10 15:00:00','2026-05-10 15:03:00');

-- ── reports ───────────────────────────────────────────────
INSERT INTO reports (report_id, generated_by, type, parameters, content, generated_at) VALUES
  (1, 13, 'MONTHLY_SALES',
   '{"month": "2026-01", "node": "all"}',
   'Total sales for January 2026: EGP 155,298.00 across 12 completed transactions. Top seller: TechZone (EGP 118,000). Top category: Electronics. Average order value: EGP 12,941.50.',
   '2026-02-01 08:00:00'),

  (2, 13, 'MONTHLY_SALES',
   '{"month": "2026-02", "node": "all"}',
   'Total sales for February 2026: EGP 107,150.00 across 8 completed transactions. Notable: 1 failed transaction (EGP 32,000). Top seller: TechZone (EGP 72,000). Category breakdown: Electronics 77%, Crafts 23%.',
   '2026-03-01 08:00:00'),

  (3, 13, 'MONTHLY_SALES',
   '{"month": "2026-03", "node": "all"}',
   'Total sales for March 2026: EGP 30,850.00 across 3 completed transactions. Withdrawal activity increased 40% vs Feb. Category breakdown: Electronics 92%, Fashion 8%.',
   '2026-04-01 08:00:00'),

  (4, 13, 'USER_ACTIVITY',
   '{"period": "2026-Q1", "metric": "new_registrations"}',
   'Q1 2026 user registrations: 7 new users. Verified rate: 71%. EXTERNAL_STORE accounts: 2 new. Total active users at end of Q1: 14.',
   '2026-04-01 09:00:00'),

  (5, 13, 'INVENTORY_ALERT',
   '{"threshold": 10, "node": "node2_cairo"}',
   'Low inventory alert — products below threshold of 10 units: Samsung 65" QLED TV (8 units). Recommend restocking within 7 days.',
   '2026-05-01 10:00:00'),

  (6, 13, 'TRANSACTION_AUDIT',
   '{"type": "REFUNDED", "period": "2026-Q2"}',
   'Refunded transactions in Q2 2026: 1 case. Transaction #24 — buyer_id 3, product_id 3 (PS5 Slim), amount EGP 24,999.00. Reason: Item arrived damaged. Refund processed within 48 hours.',
   '2026-05-06 08:00:00'),

  (7, 13, 'TOP_SELLERS',
   '{"period": "2026-YTD", "limit": 5}',
   'Year-to-date top sellers by revenue: 1) TechZone (EGP 238,298) 2) Ramy Electronics (EGP 74,598) 3) Salma Crafts (EGP 9,150) 4) Menna Boutique (EGP 5,170). Total marketplace GMV: EGP 327,216.',
   '2026-05-11 07:00:00');

SELECT setval(pg_get_serial_sequence('transactions', 'transaction_id'), COALESCE(MAX(transaction_id), 1)) FROM transactions;
SELECT setval(pg_get_serial_sequence('reports', 'report_id'), COALESCE(MAX(report_id), 1)) FROM reports;
