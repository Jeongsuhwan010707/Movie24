ALTER TABLE store_order ADD COLUMN pickup_code VARCHAR(6);
ALTER TABLE store_order ADD CONSTRAINT uk_store_order_pickup_code UNIQUE (pickup_code);
