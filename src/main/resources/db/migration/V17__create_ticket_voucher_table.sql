CREATE TABLE ticket_voucher (
    ticket_voucher_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    store_order_item_id BIGINT,
    category ENUM('TICKET','GIFT_CARD') NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    face_value INT NOT NULL,
    status ENUM('UNUSED','USED') NOT NULL,
    issued_at DATETIME NOT NULL,
    used_at DATETIME,
    reservation_id BIGINT,
    PRIMARY KEY (ticket_voucher_id),
    CONSTRAINT fk_ticket_voucher_user FOREIGN KEY (user_id) REFERENCES user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ticket_voucher_seq (
    next_val BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
INSERT INTO ticket_voucher_seq VALUES (1);

ALTER TABLE reservation ADD COLUMN voucher_discount_amount INT NOT NULL DEFAULT 0;
ALTER TABLE reservation ADD COLUMN ticket_voucher_id BIGINT;
