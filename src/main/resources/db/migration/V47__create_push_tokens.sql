-- 앱 푸시 알림을 보낼 Expo 푸시 토큰. 한 사용자가 여러 기기에서 로그인할 수 있으므로
-- 사용자당 여러 개가 있을 수 있고, 같은 토큰이 다른 사용자로 재등록되면(기기 재로그인 등)
-- unique 제약으로 소유자가 자동으로 바뀐다(UPSERT).
CREATE TABLE push_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(200) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_push_tokens_user_id ON push_tokens(user_id);
