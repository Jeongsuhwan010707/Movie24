ALTER TABLE user ADD COLUMN point INT NOT NULL DEFAULT 0;
ALTER TABLE user MODIFY COLUMN grade ENUM('ADMIN','NORMAL','SILVER','GOLD','VIP') NOT NULL DEFAULT 'NORMAL';

ALTER TABLE reservation ADD COLUMN earned_point INT NOT NULL DEFAULT 0;
ALTER TABLE reservation ADD COLUMN used_point INT NOT NULL DEFAULT 0;

CREATE TABLE point_history (
    point_history_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    reservation_id BIGINT,
    type ENUM('EARN','USE','CANCEL_EARN','CANCEL_USE') NOT NULL,
    change_amount INT NOT NULL,
    balance_after INT NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (point_history_id),
    CONSTRAINT fk_point_history_user FOREIGN KEY (user_id) REFERENCES user(user_id),
    CONSTRAINT fk_point_history_reservation FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE point_history_seq (
    next_val BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
INSERT INTO point_history_seq VALUES (1);
