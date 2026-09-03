-- 설정 > 기능 요청 게시판. 가계부 소속이 아니라 앱 전체가 공유하는 글타래라 household_id가
-- 없다. 작성자 계정이 탈퇴해도 글은 남기고(transactions.user_id와 같은 방식) 작성자만
-- "탈퇴한 사용자"로 보여준다.
CREATE TABLE feature_requests (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    admin_reply TEXT,
    replied_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_feature_requests_created_at ON feature_requests(created_at DESC);
