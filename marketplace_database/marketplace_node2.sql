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

