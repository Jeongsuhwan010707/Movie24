-- 이 파일이 실행될 일은 없다(baseline-version=1이라 V1은 베이스라인으로 처리됨).
-- 기존에 ddl-auto=update로 만들어져 있던 실제 스키마를 그대로 문서화해두고,
-- 새 환경에서 처음부터 DB를 만들 때 참고/재현할 수 있도록 남겨둔다.

CREATE TABLE user (
    user_id BIGINT NOT NULL,
    address VARCHAR(255),
    email VARCHAR(255),
    email_stauts ENUM('ALLOW','REJECT'),
    login_id VARCHAR(255),
    name VARCHAR(255),
    nick_name VARCHAR(255),
    password VARCHAR(255),
    phone VARCHAR(255),
    email_status ENUM('ALLOW','REJECT'),
    PRIMARY KEY (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_seq (
    next_val BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO user_seq VALUES (1);
