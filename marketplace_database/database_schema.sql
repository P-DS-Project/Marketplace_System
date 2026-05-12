CREATE DATABASE marketplace_node1;
\c marketplace_node1;

CREATE TABLE unique_emails (
    email VARCHAR(100) PRIMARY KEY,
    user_id INT NOT NULL
);

CREATE TABLE users (
    user_id SERIAL,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password_hash VARCHAR(256) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    role VARCHAR(20) CHECK (role IN ('USER', 'EXTERNAL_STORE', 'ADMIN')),
    avatar_url VARCHAR(500),
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id)
) PARTITION BY HASH (user_id);

CREATE TABLE users_part_0 PARTITION OF users FOR VALUES WITH (MODULUS 3, REMAINDER 0);
CREATE TABLE users_part_1 PARTITION OF users FOR VALUES WITH (MODULUS 3, REMAINDER 1);
CREATE TABLE users_part_2 PARTITION OF users FOR VALUES WITH (MODULUS 3, REMAINDER 2);

CREATE TABLE accounts (
    account_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    balance DECIMAL(15,2) DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'EGP',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE chat_messages (
    message_id SERIAL PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sender FOREIGN KEY (sender_id) REFERENCES users(user_id),
    CONSTRAINT fk_receiver FOREIGN KEY (receiver_id) REFERENCES users(user_id)
);

CREATE TABLE cart_items (
    cart_item_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1 CHECK (quantity > 0),
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT uq_cart_user_product UNIQUE (user_id, product_id)
);

CREATE INDEX idx_cart_user ON cart_items(user_id);


CREATE DATABASE marketplace_node2;
\c marketplace_node2;

CREATE TABLE categories (
    category_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT
);

CREATE TABLE products (
    product_id SERIAL,
    seller_id INT NOT NULL,
    category_id INT NOT NULL,
    name VARCHAR(200) NOT NULL,
    brand VARCHAR(100),
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    image_url VARCHAR(500),
    status VARCHAR(20) CHECK (status IN ('AVAILABLE', 'SOLD', 'REMOVED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (product_id),
    CONSTRAINT fk_category FOREIGN KEY (category_id) REFERENCES categories(category_id)
) PARTITION BY HASH (product_id);

CREATE TABLE products_part_0 PARTITION OF products FOR VALUES WITH (MODULUS 3, REMAINDER 0);
CREATE TABLE products_part_1 PARTITION OF products FOR VALUES WITH (MODULUS 3, REMAINDER 1);
CREATE TABLE products_part_2 PARTITION OF products FOR VALUES WITH (MODULUS 3, REMAINDER 2);

CREATE TABLE inventory (
    inventory_id SERIAL PRIMARY KEY,
    product_id INT NOT NULL UNIQUE,
    quantity INT NOT NULL CHECK (quantity >= 0),
    warehouse_node VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES products(product_id)
);


CREATE DATABASE marketplace_node3;
\c marketplace_node3;

CREATE TABLE transactions (
    transaction_id SERIAL,
    buyer_id INT NOT NULL,
    seller_id INT,
    product_id INT,
    quantity INT,
    amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED')),
    type VARCHAR(20) CHECK (type IN ('PURCHASE', 'DEPOSIT', 'WITHDRAWAL')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    PRIMARY KEY (transaction_id, created_at)
) PARTITION BY RANGE (created_at);

CREATE TABLE transactions_2026_q1 PARTITION OF transactions FOR VALUES FROM ('2026-01-01') TO ('2026-04-01');
CREATE TABLE transactions_2026_q2 PARTITION OF transactions FOR VALUES FROM ('2026-04-01') TO ('2026-07-01');
CREATE TABLE transactions_default PARTITION OF transactions DEFAULT;

CREATE TABLE reports (
    report_id SERIAL PRIMARY KEY,
    generated_by INT NOT NULL,
    type VARCHAR(50) NOT NULL,
    parameters JSON,
    content TEXT,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);