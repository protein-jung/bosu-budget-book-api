-- 통계(월별 요약) 화면 맨 위에서 가계부 구성원이 그 달에 대해 남기는 코멘트.
CREATE TABLE month_comments (
    id BIGSERIAL PRIMARY KEY,
    household_id BIGINT NOT NULL REFERENCES households(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    year INT NOT NULL,
    month INT NOT NULL,
    body VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_month_comments_household_year_month ON month_comments(household_id, year, month);
