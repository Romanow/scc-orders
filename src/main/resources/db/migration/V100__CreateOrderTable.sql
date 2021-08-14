-- V100: create order table
CREATE TABLE orders
(
    id         SERIAL PRIMARY KEY,
    uid        uuid   NOT NULL,
    items_uids uuid[] NOT NULL,
    first_name VARCHAR(80),
    last_name  VARCHAR(80),
    address    VARCHAR(80)
);

CREATE UNIQUE INDEX idx_orders_uid ON orders (uid);