-- user 엔티티를 GenerationType.AUTO(테이블 기반 시퀀스 흉내, allocationSize 기본값 50)에서
-- GenerationType.IDENTITY(MySQL auto_increment)로 전환한다.
-- 재시작할 때마다 미리 50개씩 예약해두던 id가 안 쓰이고 버려지면서 user_id가 듬성듬성 증가하던 문제를 없앤다.
-- AUTO_INCREMENT를 붙이면 MySQL이 기존 데이터의 최댓값+1부터 자동으로 이어서 채번한다.
-- reservation.fk_reservation_user가 user_id를 참조하고 있어서, FK가 걸린 채로는
-- MySQL이 AUTO_INCREMENT 추가를 in-place로 거부한다(ERROR 1833) - 변경하는 동안만 FK 체크를 끈다.
SET FOREIGN_KEY_CHECKS = 0;
ALTER TABLE user MODIFY user_id BIGINT NOT NULL AUTO_INCREMENT;
SET FOREIGN_KEY_CHECKS = 1;

-- user 엔티티 전용이었던 시퀀스 흉내 테이블이라 더 이상 필요 없다.
DROP TABLE user_seq;
