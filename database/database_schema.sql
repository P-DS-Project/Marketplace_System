CREATE TABLE users (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(256) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    role VARCHAR(20) CHECK (role IN ('BUYER', 'SELLER', 'ADMIN', 'EXTERNAL_STORE')),
    is_verified BIT DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

CREATE TABLE accounts (
    account_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    balance DECIMAL(15,2) DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'EGP',
    updated_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE chat_messages (
    message_id INT IDENTITY(1,1) PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    content TEXT NOT NULL,
    is_read BIT DEFAULT 0,
    timestamp DATETIME DEFAULT GETDATE(),
    CONSTRAINT fk_sender FOREIGN KEY (sender_id) REFERENCES users(user_id),
    CONSTRAINT fk_receiver FOREIGN KEY (receiver_id) REFERENCES users(user_id)
);

CREATE TABLE categories (
    category_id INT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT
);

CREATE TABLE products (
    product_id INT IDENTITY(1,1) PRIMARY KEY,
    seller_id INT NOT NULL,
    category_id INT NOT NULL,
    name VARCHAR(200) NOT NULL,
    brand VARCHAR(100),
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) CHECK (status IN ('AVAILABLE', 'SOLD', 'REMOVED')),
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT fk_category FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

CREATE TABLE inventory (
    inventory_id INT IDENTITY(1,1) PRIMARY KEY,
    product_id INT NOT NULL UNIQUE,
    quantity INT NOT NULL CHECK (quantity >= 0),
    warehouse_node VARCHAR(50),
    updated_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES products(product_id)
);

CREATE TABLE transactions (
    transaction_id INT IDENTITY(1,1) PRIMARY KEY,
    buyer_id INT NOT NULL,
    seller_id INT,
    product_id INT,
    quantity INT,
    amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED')),
    type VARCHAR(20) CHECK (type IN ('PURCHASE', 'DEPOSIT', 'WITHDRAWAL')),
    created_at DATETIME DEFAULT GETDATE(),
    completed_at DATETIME
);

CREATE TABLE reports (
    report_id INT IDENTITY(1,1) PRIMARY KEY,
    generated_by INT NOT NULL,
    type VARCHAR(50) NOT NULL,
    parameters NVARCHAR(MAX),
    content TEXT,
    generated_at DATETIME DEFAULT GETDATE()
);