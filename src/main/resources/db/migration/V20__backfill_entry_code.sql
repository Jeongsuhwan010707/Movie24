-- V19에서 entry_code 컬럼을 추가했지만, 그 이전에 만들어진 예매/주문에는 값이 채워지지 않는다.
-- 기존 데이터에도 소급 적용해 마이페이지에서 바로 코드를 확인할 수 있게 한다.
UPDATE reservation
SET entry_code = CONCAT(
    UPPER(SUBSTRING(MD5(RAND())FROM 1 FOR 4)), '-',
    UPPER(SUBSTRING(MD5(RAND())FROM 1 FOR 4)), '-',
    UPPER(SUBSTRING(MD5(RAND())FROM 1 FOR 4)), '-',
    UPPER(SUBSTRING(MD5(RAND())FROM 1 FOR 4))
)
WHERE entry_code IS NULL;

UPDATE store_order
SET entry_code = CONCAT(
    UPPER(SUBSTRING(MD5(RAND())FROM 1 FOR 4)), '-',
    UPPER(SUBSTRING(MD5(RAND())FROM 1 FOR 4)), '-',
    UPPER(SUBSTRING(MD5(RAND())FROM 1 FOR 4)), '-',
    UPPER(SUBSTRING(MD5(RAND())FROM 1 FOR 4))
)
WHERE entry_code IS NULL;
