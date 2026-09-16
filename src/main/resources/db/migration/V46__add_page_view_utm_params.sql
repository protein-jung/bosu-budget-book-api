-- 마케팅 유입 추적. https://bosuledger.com/welcome?utm_source=instagram&utm_medium=social&utm_campaign=profile
-- 처럼 URL에 붙는 UTM 파라미터를 방문 기록과 함께 저장해 어드민 접속 통계에서 유입 채널별로
-- 집계할 수 있게 한다. 직접 방문/일반 앱 사용에는 없는 값이라 전부 nullable이다.
ALTER TABLE page_views ADD COLUMN utm_source VARCHAR(100);
ALTER TABLE page_views ADD COLUMN utm_medium VARCHAR(100);
ALTER TABLE page_views ADD COLUMN utm_campaign VARCHAR(100);
