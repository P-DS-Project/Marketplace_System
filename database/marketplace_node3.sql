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