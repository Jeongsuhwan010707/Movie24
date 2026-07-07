CREATE TABLE movie (
    movie_id BIGINT NOT NULL,
    title VARCHAR(255),
    director VARCHAR(255),
    actors VARCHAR(255),
    genre VARCHAR(255),
    age_rating ENUM('ALL','AGE_12','AGE_15','AGE_18'),
    runtime_minutes INT,
    country VARCHAR(255),
    release_date DATE,
    synopsis TEXT,
    poster_image_url VARCHAR(255),
    trailer_video_url VARCHAR(255),
    status ENUM('COMING_SOON','NOW_SHOWING','ENDED'),
    PRIMARY KEY (movie_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE movie_seq (
    next_val BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO movie_seq VALUES (1);
