-- 헤더 검색창에 실제로 타이핑한 검색어 로그. 어드민 "접속 통계" 화면에서 어떤 검색어가
-- 많이 쓰이는지 집계할 때 쓴다. 날짜만으로 검색한 경우(검색어 없음)는 기록하지 않는다.
CREATE TABLE search_logs (
    id BIGSERIAL PRIMARY KEY,
    query VARCHAR(255) NOT NULL,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_search_logs_created_at ON search_logs(created_at);
