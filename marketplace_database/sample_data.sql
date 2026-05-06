\c marketplace_node1;

INSERT INTO users (username, email, password_hash, salt, role, is_verified) VALUES
('seif_buyer', 'seif@example.com', 'hashed_pw_1', 'salt_1', 'BUYER', true),
('sherpiny_seller', 'sherpiny@example.com', 'hashed_pw_2', 'salt_2', 'SELLER', true),
('admin_boss', 'admin@example.com', 'hashed_pw_3', 'salt_3', 'ADMIN', true);

INSERT INTO accounts (user_id, balance, currency) VALUES
(1, 15000.00, 'EGP'),
(2, 50000.00, 'EGP'),
(3, 0.00, 'EGP');

INSERT INTO chat_messages (sender_id, receiver_id, content, is_read) VALUES
(1, 2, 'Is the laptop available?', true),
(2, 1, 'Yes, it is available and ready for shipping.', false);

\c marketplace_node2;

INSERT INTO categories (name, description) VALUES
('Electronics', 'Laptops, mobile phones, and consumer electronics'),
('Gaming', 'Gaming consoles and accessories');

INSERT INTO products (seller_id, category_id, name, brand, description, price, status) VALUES
(2, 1, 'Gaming Laptop', 'Dell', '16GB RAM, 1TB SSD, RTX 4060', 35000.00, 'AVAILABLE'),
(2, 2, 'PlayStation 5', 'Sony', 'Disk edition with an extra controller', 28000.00, 'AVAILABLE');

INSERT INTO inventory (product_id, quantity, warehouse_node) VALUES
(1, 10, 'Cairo_Warehouse_1'),
(2, 5, 'Giza_Warehouse_2');

\c marketplace_node3;

INSERT INTO transactions (buyer_id, seller_id, product_id, quantity, amount, status, type, created_at, completed_at) VALUES
(1, NULL, NULL, NULL, 15000.00, 'COMPLETED', 'DEPOSIT', '2026-02-10 14:00:00', '2026-02-10 14:05:00'),
(1, 2, 1, 1, 35000.00, 'COMPLETED', 'PURCHASE', '2026-02-15 10:30:00', '2026-02-15 10:31:00'),
(1, 2, 2, 1, 28000.00, 'PENDING', 'PURCHASE', '2026-05-20 18:00:00', NULL);

INSERT INTO reports (generated_by, type, parameters, content, generated_at) VALUES
(3, 'SALES_SUMMARY', '{"month": "February", "year": 2026}', 'Total sales for February: 35000 EGP', '2026-02-28 23:59:59');