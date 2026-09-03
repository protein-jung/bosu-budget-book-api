-- 기능 요청 답변 알림, 가계부 구성원의 거래 등록 알림 등을 담는 테이블. 여기 recipient_id는
-- feature_requests.user_id(글쓴이 표시용, SET NULL)와 다르게 "이 알림을 누가 보는가" 그 자체라서
-- 대상자가 탈퇴하면 알림도 같이 지운다(CASCADE).
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    body VARCHAR(500),
    link VARCHAR(200),
    read BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_notifications_recipient_created_at ON notifications(recipient_id, created_at DESC);
CREATE INDEX idx_notifications_recipient_unread ON notifications(recipient_id) WHERE NOT read;
