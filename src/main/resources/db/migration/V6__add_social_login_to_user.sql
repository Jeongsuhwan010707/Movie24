ALTER TABLE user ADD COLUMN provider ENUM('LOCAL','KAKAO','NAVER','GOOGLE') NOT NULL DEFAULT 'LOCAL';
ALTER TABLE user ADD COLUMN provider_id VARCHAR(255);
ALTER TABLE user ADD CONSTRAINT uk_user_provider_provider_id UNIQUE (provider, provider_id);
