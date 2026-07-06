-- ddl-auto=update가 오랫동안 컬럼을 지우지 못해 남아있던 오타 컬럼 정리,
-- 엔티티(@Column(unique = true))에는 있었지만 실제 DB에는 반영된 적 없던 유니크 제약 추가.

ALTER TABLE user DROP COLUMN email_stauts;
ALTER TABLE user ADD CONSTRAINT uk_user_login_id UNIQUE (login_id);
