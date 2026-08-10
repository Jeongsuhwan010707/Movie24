CREATE TABLE event (
    event_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    thumbnail_url VARCHAR(255),
    status ENUM('ONGOING','ENDED') NOT NULL,
    winner_announced BOOLEAN NOT NULL DEFAULT FALSE,
    winner_content TEXT,
    start_date DATE,
    end_date DATE,
    display_order INT NOT NULL DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME,
    PRIMARY KEY (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE event_seq (
    next_val BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
INSERT INTO event_seq VALUES (1);

-- 시드 데이터는 Hibernate가 event_seq에서 발급하는 id(1부터)와 절대 겹치지 않도록
-- 충분히 큰 id 대역(9001~)을 직접 지정해 넣는다.
INSERT INTO event (event_id, title, content, thumbnail_url, status, winner_announced, winner_content, start_date, end_date, display_order, created_at, updated_at) VALUES
(9001, '여름맞이 팝콘 반값 이벤트', '무비24 앱 회원이라면 누구나 팝콘 세트를 반값에 즐길 수 있는 이벤트입니다. 매표소 또는 스토어 결제 시 쿠폰함에서 쿠폰을 다운로드해 사용해주세요.', '/resources/images/이벤트1열1번.jpg', 'ONGOING', false, null, '2026-07-15', '2026-09-15', 1, NOW(), NOW()),
(9002, '신규 회원 웰컴 쿠폰팩', '무비24에 처음 가입한 회원에게 예매 할인 쿠폰 3장과 스토어 할인 쿠폰 1장을 드립니다. 마이페이지 > 쿠폰함에서 바로 확인하세요.', '/resources/images/이벤트1열2번.jpg', 'ONGOING', false, null, '2026-07-01', '2026-12-31', 2, NOW(), NOW()),
(9003, '관람평 작성하고 굿즈 받기', '관람하신 영화에 리뷰를 남겨주시면 추첨을 통해 오리지널 굿즈를 드립니다. 한 달 동안 등록된 리뷰 중 우수 리뷰를 선정합니다.', '/resources/images/이벤트1열3번.jpg', 'ONGOING', false, null, '2026-08-01', '2026-08-31', 3, NOW(), NOW()),
(9004, '생일 축하 무료 티켓', '생일이 있는 달에 로그인하면 무료 관람권 1매를 드립니다. 마이페이지에 생년월일을 등록한 회원만 참여할 수 있습니다.', '/resources/images/이벤트2열1번.jpg', 'ONGOING', false, null, '2026-01-01', '2026-12-31', 4, NOW(), NOW()),
(9005, '친구 초대 이벤트', '친구를 초대해 회원가입이 완료되면 초대한 회원과 가입한 친구 모두에게 예매 할인 쿠폰을 드립니다.', '/resources/images/이벤트2열2번.jpg', 'ONGOING', false, null, '2026-07-20', '2026-09-30', 5, NOW(), NOW()),
(9006, '멤버십 등급 업 이벤트', '이번 달 예매 3회 이상 시 다음 달 멤버십 등급이 자동으로 상승합니다. 등급이 오르면 스토어 적립 혜택도 함께 늘어납니다.', '/resources/images/이벤트맨끝.jpg', 'ONGOING', false, null, '2026-08-01', '2026-08-31', 6, NOW(), NOW()),
(9007, '오펜하이머 개봉 기념 이벤트', '오펜하이머 개봉을 기념해 예매 인증 시 추첨을 통해 한정판 포스터를 증정했습니다. 많은 참여 감사드립니다.', '/resources/images/event오펜하이머.jpg', 'ENDED', true, '당첨자는 개별적으로 문자 안내를 드렸습니다. 당첨자 아이디: movi***, cine***, popc*** 외 27명. 경품은 등록하신 주소로 순차 발송됩니다.', '2026-05-01', '2026-06-15', 7, NOW(), NOW()),
(9008, '존윅4 특전 증정 이벤트', '존윅4 예매 관객 전원에게 선착순으로 스페셜 포토카드를 증정했던 이벤트입니다.', '/resources/images/event존윅4.jpg', 'ENDED', true, '준비된 포토카드가 모두 소진되어 이벤트가 조기 종료되었습니다. 참여해주신 모든 분들께 감사드립니다.', '2026-05-10', '2026-06-10', 8, NOW(), NOW()),
(9009, 'KT 멤버십 할인 이벤트', 'KT 멤버십 회원 대상으로 예매 시 최대 4천원 할인을 제공했던 제휴 이벤트입니다.', '/resources/images/eventKT멤버십.jpg', 'ENDED', false, null, '2026-04-01', '2026-05-31', 9, NOW(), NOW()),
(9010, 'M포인트 적립 이벤트', '스토어에서 결제 시 M포인트를 2배로 적립해드리는 이벤트입니다. 적립된 포인트는 다음 결제 시 현금처럼 사용할 수 있습니다.', '/resources/images/eventM포인트.jpg', 'ONGOING', false, null, '2026-08-01', '2026-09-30', 10, NOW(), NOW());
