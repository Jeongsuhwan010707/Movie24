-- 매장 수령용 6자리 코드(pickup_code)를, 예매/구매 공통으로 쓰는 "XXXX-XXXX-XXXX-XXXX" 형태의
-- 키오스크 입력 코드(entry_code)로 대체한다. 아직 실제 서비스 데이터가 아니라 그대로 교체한다.
ALTER TABLE store_order DROP INDEX uk_store_order_pickup_code;
ALTER TABLE store_order DROP COLUMN pickup_code;
ALTER TABLE store_order ADD COLUMN entry_code VARCHAR(19);
ALTER TABLE store_order ADD CONSTRAINT uk_store_order_entry_code UNIQUE (entry_code);

ALTER TABLE reservation ADD COLUMN entry_code VARCHAR(19);
ALTER TABLE reservation ADD CONSTRAINT uk_reservation_entry_code UNIQUE (entry_code);
