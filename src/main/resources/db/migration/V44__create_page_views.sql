-- 어드민 "접속 통계" 화면에서 볼, 프론트 전체 페이지(로그인·회원가입·웰컴 등 인증 전 페이지
-- 포함) 방문 로그. visitor_id는 기기/브라우저에 저장된 익명 식별자로, 로그인 여부와 무관하게
-- 항상 채워진다 — 순 방문자 수 집계에 쓴다.
CREATE TABLE page_views (
    id BIGSERIAL PRIMARY KEY,
    path VARCHAR(255) NOT NULL,
    visitor_id VARCHAR(64) NOT NULL,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_page_views_path ON page_views(path);
CREATE INDEX idx_page_views_created_at ON page_views(created_at);
