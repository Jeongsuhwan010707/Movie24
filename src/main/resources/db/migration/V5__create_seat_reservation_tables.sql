CREATE TABLE seat (
    seat_id BIGINT NOT NULL,
    screen_id BIGINT,
    row_label VARCHAR(255),
    seat_number INT,
    PRIMARY KEY (seat_id),
    CONSTRAINT fk_seat_screen FOREIGN KEY (screen_id) REFERENCES screen(screen_id),
    CONSTRAINT uk_seat_screen_row_number UNIQUE (screen_id, row_label, seat_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE seat_seq (
    next_val BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
INSERT INTO seat_seq VALUES (1);

CREATE TABLE reservation (
    reservation_id BIGINT NOT NULL,
    user_id BIGINT,
    showtime_id BIGINT,
    total_price INT,
    status ENUM('RESERVED','CANCELLED'),
    reserved_at DATETIME,
    PRIMARY KEY (reservation_id),
    CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES user(user_id),
    CONSTRAINT fk_reservation_showtime FOREIGN KEY (showtime_id) REFERENCES showtime(showtime_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE reservation_seq (
    next_val BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
INSERT INTO reservation_seq VALUES (1);

-- showtime_id + seat_id 유니크 제약이 동시 예매로 인한 좌석 중복 예약을 DB 레벨에서 막아준다.
CREATE TABLE reservation_seat (
    reservation_seat_id BIGINT NOT NULL,
    reservation_id BIGINT,
    seat_id BIGINT,
    showtime_id BIGINT,
    PRIMARY KEY (reservation_seat_id),
    CONSTRAINT fk_reservation_seat_reservation FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id),
    CONSTRAINT fk_reservation_seat_seat FOREIGN KEY (seat_id) REFERENCES seat(seat_id),
    CONSTRAINT fk_reservation_seat_showtime FOREIGN KEY (showtime_id) REFERENCES showtime(showtime_id),
    CONSTRAINT uk_reservation_seat_showtime_seat UNIQUE (showtime_id, seat_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE reservation_seat_seq (
    next_val BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
INSERT INTO reservation_seat_seq VALUES (1);
