-- 설정 > 리뷰 남기기. 가계부 소속이 아니라 앱 전체에 대한 리뷰라 household_id가 없고,
-- 개수 제한 없이 다시 남길 수 있어 매번 새 행으로 쌓인다. 작성자 계정이 탈퇴해도 리뷰는
-- 남기고(feature_requests.user_id와 같은 방식) 작성자만 "탈퇴한 사용자"로 보여준다.
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    rating INTEGER NOT NULL,
    content TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_reviews_created_at ON reviews(created_at DESC);
CREATE INDEX idx_reviews_user_id ON reviews(user_id);
