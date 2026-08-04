CREATE TABLE store_item (
    store_item_id BIGINT NOT NULL,
    category ENUM('PACKAGE','TICKET','GIFT_CARD') NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    price INT NOT NULL,
    image_url VARCHAR(255),
    display_order INT NOT NULL DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME,
    PRIMARY KEY (store_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE store_item_seq (
    next_val BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
INSERT INTO store_item_seq VALUES (1);

-- 시드 데이터는 Hibernate가 store_item_seq에서 발급하는 id(1부터)와 절대 겹치지 않도록
-- 충분히 큰 id 대역(9001~)을 직접 지정해 넣는다.
INSERT INTO store_item (store_item_id, category, name, description, price, image_url, display_order, created_at, updated_at) VALUES
(9001, 'PACKAGE', '팝콘 패키지1', '팝콘(1), 음료(1), 관람권(1)', 10000, '/resources/images/cgv팝콘-removebg-preview.png', 1, NOW(), NOW()),
(9002, 'PACKAGE', '팝콘 패키지2', '팝콘(1), 음료(2)', 10000, '/resources/images/cgv팝콘2-removebg-preview.png', 2, NOW(), NOW()),
(9003, 'PACKAGE', '팝콘 패키지3', '팝콘(2), 음료(2)', 10000, '/resources/images/메가박스팝콘.png', 3, NOW(), NOW()),
(9004, 'PACKAGE', '팝콘 패키지4', '팝콘(1), 음료(1), 나쵸(1)', 10000, '/resources/images/메가박스팝콘2.png', 4, NOW(), NOW()),
(9005, 'TICKET', '영화관람권 1', '전 관, 전 시간 사용 가능', 20000, '/resources/images/메가티켓1.png', 1, NOW(), NOW()),
(9006, 'TICKET', '영화관람권 2', '전 관, 전 시간 사용 가능', 30000, '/resources/images/메가티켓2.png', 2, NOW(), NOW()),
(9007, 'TICKET', '영화관람권 3', '2D 전용', 10000, '/resources/images/메가티켓3.png', 3, NOW(), NOW()),
(9008, 'TICKET', '영화관람권 4', '조조 전용', 15000, '/resources/images/메가티켓4.png', 4, NOW(), NOW()),
(9009, 'GIFT_CARD', '기프트카드 1', '전국 매장 공통 사용', 11000, '/resources/images/메가카드1.png', 1, NOW(), NOW()),
(9010, 'GIFT_CARD', '기프트카드 2', '전국 매장 공통 사용', 12000, '/resources/images/메가카드2.png', 2, NOW(), NOW()),
(9011, 'GIFT_CARD', '기프트카드 3', '전국 매장 공통 사용', 20000, '/resources/images/메가카드3.png', 3, NOW(), NOW());

CREATE TABLE cart_item (
    cart_item_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    store_item_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    created_at DATETIME,
    updated_at DATETIME,
    PRIMARY KEY (cart_item_id),
    CONSTRAINT fk_cart_item_user FOREIGN KEY (user_id) REFERENCES user(user_id),
    CONSTRAINT fk_cart_item_store_item FOREIGN KEY (store_item_id) REFERENCES store_item(store_item_id),
    CONSTRAINT uk_cart_item_user_store_item UNIQUE (user_id, store_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE cart_item_seq (
    next_val BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
INSERT INTO cart_item_seq VALUES (1);

CREATE TABLE store_order (
    store_order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_uid VARCHAR(64) NOT NULL,
    total_amount INT NOT NULL,
    ordered_at DATETIME,
    PRIMARY KEY (store_order_id),
    CONSTRAINT fk_store_order_user FOREIGN KEY (user_id) REFERENCES user(user_id),
    CONSTRAINT uk_store_order_order_uid UNIQUE (order_uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE store_order_seq (
    next_val BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
INSERT INTO store_order_seq VALUES (1);

-- 주문 시점의 상품명/단가를 스냅샷으로 남겨, 이후 store_item이 수정/삭제돼도 과거 주문 내역이 바뀌지 않게 한다.
CREATE TABLE store_order_item (
    store_order_item_id BIGINT NOT NULL,
    store_order_id BIGINT NOT NULL,
    store_item_id BIGINT NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    unit_price INT NOT NULL,
    quantity INT NOT NULL,
    PRIMARY KEY (store_order_item_id),
    CONSTRAINT fk_store_order_item_order FOREIGN KEY (store_order_id) REFERENCES store_order(store_order_id),
    CONSTRAINT fk_store_order_item_item FOREIGN KEY (store_item_id) REFERENCES store_item(store_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE store_order_item_seq (
    next_val BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
INSERT INTO store_order_item_seq VALUES (1);
