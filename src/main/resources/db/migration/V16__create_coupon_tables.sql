CREATE TABLE coupon (
    coupon_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    discount_type ENUM('PERCENT','AMOUNT') NOT NULL,
    discount_value INT NOT NULL,
    max_discount_amount INT,
    min_purchase_amount INT NOT NULL DEFAULT 0,
    applicable_context ENUM('RESERVATION','STORE','BOTH') NOT NULL,
    code VARCHAR(50),
    event_id BIGINT,
    valid_days INT,
    valid_from DATETIME,
    valid_until DATETIME,
    total_quantity INT,
    issued_quantity INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME,
    updated_at DATETIME,
    PRIMARY KEY (coupon_id),
    CONSTRAINT uk_coupon_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE coupon_seq (
    next_val BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
INSERT INTO coupon_seq VALUES (1);

CREATE TABLE user_coupon (
    user_coupon_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    coupon_id BIGINT NOT NULL,
    status ENUM('UNUSED','USED','EXPIRED') NOT NULL,
    issued_at DATETIME NOT NULL,
    expires_at DATETIME,
    used_at DATETIME,
    reservation_id BIGINT,
    discount_amount_applied INT,
    PRIMARY KEY (user_coupon_id),
    CONSTRAINT fk_user_coupon_user FOREIGN KEY (user_id) REFERENCES user(user_id),
    CONSTRAINT fk_user_coupon_coupon FOREIGN KEY (coupon_id) REFERENCES coupon(coupon_id),
    CONSTRAINT uk_user_coupon_user_coupon UNIQUE (user_id, coupon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_coupon_seq (
    next_val BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
INSERT INTO user_coupon_seq VALUES (1);

ALTER TABLE reservation ADD COLUMN coupon_discount_amount INT NOT NULL DEFAULT 0;
ALTER TABLE reservation ADD COLUMN user_coupon_id BIGINT;
