CREATE TABLE theater (
    theater_id BIGINT NOT NULL,
    name VARCHAR(255),
    region VARCHAR(255),
    address VARCHAR(255),
    PRIMARY KEY (theater_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE theater_seq (
    next_val BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
INSERT INTO theater_seq VALUES (1);

CREATE TABLE screen (
    screen_id BIGINT NOT NULL,
    theater_id BIGINT,
    name VARCHAR(255),
    total_seats INT,
    screen_type VARCHAR(255),
    PRIMARY KEY (screen_id),
    CONSTRAINT fk_screen_theater FOREIGN KEY (theater_id) REFERENCES theater(theater_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE screen_seq (
    next_val BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
INSERT INTO screen_seq VALUES (1);

CREATE TABLE showtime (
    showtime_id BIGINT NOT NULL,
    movie_id BIGINT,
    screen_id BIGINT,
    start_time DATETIME,
    base_price INT,
    PRIMARY KEY (showtime_id),
    CONSTRAINT fk_showtime_movie FOREIGN KEY (movie_id) REFERENCES movie(movie_id),
    CONSTRAINT fk_showtime_screen FOREIGN KEY (screen_id) REFERENCES screen(screen_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE showtime_seq (
    next_val BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
INSERT INTO showtime_seq VALUES (1);
