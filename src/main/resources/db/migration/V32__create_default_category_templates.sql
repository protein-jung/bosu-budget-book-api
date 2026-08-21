CREATE TABLE default_category_templates (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(20),
    icon VARCHAR(8),
    parent_id BIGINT REFERENCES default_category_templates(id) ON DELETE CASCADE,
    is_group BOOLEAN NOT NULL DEFAULT FALSE,
    excluded_from_expense_stats BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_default_category_templates_parent_id ON default_category_templates(parent_id);

-- CategoryDefaultSeeder.java에 하드코딩돼 있던 doslxk@gmail.com 가계부 구조를 그대로 옮긴다.
-- 최상위 항목(parent_id NULL)은 그룹과 단독 리프가 sort_order 하나의 순서를 공유한다.
INSERT INTO default_category_templates (type, name, color, icon, parent_id, is_group, excluded_from_expense_stats, sort_order) VALUES
('EXPENSE', '미래준비', '#7048e8', '💰', NULL, true, true, 0),
('EXPENSE', '장보기', '#f08c00', '🛒', NULL, true, false, 1),
('EXPENSE', '외식', '#e03131', NULL, NULL, true, false, 2),
('EXPENSE', '의료', '#e03131', '🏥', NULL, true, false, 3),
('EXPENSE', '오락/취미', '#7048e8', '🎮', NULL, false, false, 4),
('EXPENSE', '개인지출', '#1971c2', '👛', NULL, true, false, 5),
('EXPENSE', '꾸밈비', '#e64980', '💄', NULL, true, false, 6),
('EXPENSE', '교통비', '#e03131', '🚗', NULL, true, false, 7),
('EXPENSE', '주거비', '#0c8599', '🏠', NULL, true, false, 8),
('EXPENSE', '세금', '#e8590c', '🧾', NULL, true, false, 9),
('EXPENSE', '매월 구독비용', '#495057', '💳', NULL, true, false, 10),
('EXPENSE', '통신비', '#1971c2', '📱', NULL, false, false, 11),
('EXPENSE', '특수지출', '#e03131', '⛽', NULL, false, false, 12),
('INCOME', '수민월급', '#e03131', '🏦', NULL, false, false, 0),
('INCOME', '보영 월급', '#e03131', '🏦', NULL, false, false, 1),
('INCOME', '육아휴직급여', '#e03131', '👶', NULL, false, false, 2),
('INCOME', '부모급여', '#e03131', '👶', NULL, false, false, 3),
('INCOME', '아동수당', '#e03131', '👶', NULL, false, false, 4),
('INCOME', '부수입', '#e03131', '💵', NULL, false, false, 5),
('INCOME', '당근', '#e03131', '📦', NULL, false, false, 6),
('INCOME', '보험 환급', '#e03131', '💰', NULL, false, false, 7),
('INCOME', '기타 수입', '#e03131', '❓', NULL, false, false, 8);

INSERT INTO default_category_templates (type, name, color, icon, parent_id, is_group, excluded_from_expense_stats, sort_order)
SELECT 'EXPENSE', v.name, v.color, v.icon, p.id, false, p.excluded_from_expense_stats, v.sort_order
FROM (SELECT id, excluded_from_expense_stats FROM default_category_templates WHERE name = '미래준비' AND parent_id IS NULL) p,
(VALUES
  ('적금', '#e03131', '💵', 0),
  ('예금', '#7048e8', '🏦', 1),
  ('IRP', '#e03131', '💵', 2),
  ('주식', '#e03131', '📈', 3),
  ('코인', '#e03131', '📈', 4)
) AS v(name, color, icon, sort_order);

INSERT INTO default_category_templates (type, name, color, icon, parent_id, is_group, excluded_from_expense_stats, sort_order)
SELECT 'EXPENSE', v.name, v.color, v.icon, p.id, false, p.excluded_from_expense_stats, v.sort_order
FROM (SELECT id, excluded_from_expense_stats FROM default_category_templates WHERE name = '장보기' AND parent_id IS NULL) p,
(VALUES
  ('생필품', '#f08c00', '🧻', 0),
  ('식료품', '#2f9e44', '🥬', 1),
  ('육아', '#f08c00', '👶', 2)
) AS v(name, color, icon, sort_order);

INSERT INTO default_category_templates (type, name, color, icon, parent_id, is_group, excluded_from_expense_stats, sort_order)
SELECT 'EXPENSE', v.name, v.color, v.icon, p.id, false, p.excluded_from_expense_stats, v.sort_order
FROM (SELECT id, excluded_from_expense_stats FROM default_category_templates WHERE name = '외식' AND parent_id IS NULL) p,
(VALUES
  ('카페', '#7048e8', '☕', 0),
  ('배달음식', '#f08c00', '🛵', 1),
  ('음식점', '#e64980', '🍽️', 2)
) AS v(name, color, icon, sort_order);

INSERT INTO default_category_templates (type, name, color, icon, parent_id, is_group, excluded_from_expense_stats, sort_order)
SELECT 'EXPENSE', v.name, v.color, v.icon, p.id, false, p.excluded_from_expense_stats, v.sort_order
FROM (SELECT id, excluded_from_expense_stats FROM default_category_templates WHERE name = '의료' AND parent_id IS NULL) p,
(VALUES
  ('병원비', '#e03131', '🏥', 0),
  ('약국', '#e03131', '💊', 1),
  ('영양제', '#e03131', '🌿', 2),
  ('보험비', '#e03131', '🧾', 3)
) AS v(name, color, icon, sort_order);

INSERT INTO default_category_templates (type, name, color, icon, parent_id, is_group, excluded_from_expense_stats, sort_order)
SELECT 'EXPENSE', v.name, v.color, v.icon, p.id, false, p.excluded_from_expense_stats, v.sort_order
FROM (SELECT id, excluded_from_expense_stats FROM default_category_templates WHERE name = '개인지출' AND parent_id IS NULL) p,
(VALUES
  ('보영용돈', '#e03131', '💰', 0),
  ('수민용돈', '#e03131', '💰', 1),
  ('교육', '#e64980', '📚', 2)
) AS v(name, color, icon, sort_order);

INSERT INTO default_category_templates (type, name, color, icon, parent_id, is_group, excluded_from_expense_stats, sort_order)
SELECT 'EXPENSE', v.name, v.color, v.icon, p.id, false, p.excluded_from_expense_stats, v.sort_order
FROM (SELECT id, excluded_from_expense_stats FROM default_category_templates WHERE name = '꾸밈비' AND parent_id IS NULL) p,
(VALUES
  ('미용', '#e64980', '💇', 0),
  ('쇼핑', '#7048e8', '📦', 1)
) AS v(name, color, icon, sort_order);

INSERT INTO default_category_templates (type, name, color, icon, parent_id, is_group, excluded_from_expense_stats, sort_order)
SELECT 'EXPENSE', v.name, v.color, v.icon, p.id, false, p.excluded_from_expense_stats, v.sort_order
FROM (SELECT id, excluded_from_expense_stats FROM default_category_templates WHERE name = '교통비' AND parent_id IS NULL) p,
(VALUES
  ('자동차 충전비', '#e03131', '⚡️', 0),
  ('톨게이트 비용', '#2f9e44', '🛣️', 1),
  ('자동차 보험비', '#e03131', '🚗', 2),
  ('주차비', '#e03131', '🚗', 3),
  ('대중교통', '#1971c2', '🚌', 4)
) AS v(name, color, icon, sort_order);

INSERT INTO default_category_templates (type, name, color, icon, parent_id, is_group, excluded_from_expense_stats, sort_order)
SELECT 'EXPENSE', v.name, v.color, v.icon, p.id, false, p.excluded_from_expense_stats, v.sort_order
FROM (SELECT id, excluded_from_expense_stats FROM default_category_templates WHERE name = '주거비' AND parent_id IS NULL) p,
(VALUES
  ('관리비', '#0c8599', '🏠', 0),
  ('집 원금', '#e03131', '🏠', 1),
  ('집 이자', '#e03131', '🏠', 2)
) AS v(name, color, icon, sort_order);

INSERT INTO default_category_templates (type, name, color, icon, parent_id, is_group, excluded_from_expense_stats, sort_order)
SELECT 'EXPENSE', v.name, v.color, v.icon, p.id, false, p.excluded_from_expense_stats, v.sort_order
FROM (SELECT id, excluded_from_expense_stats FROM default_category_templates WHERE name = '세금' AND parent_id IS NULL) p,
(VALUES
  ('자동차 세금', '#e03131', '🧾', 0),
  ('아파트 세금', '#e8590c', '🧾', 1)
) AS v(name, color, icon, sort_order);

INSERT INTO default_category_templates (type, name, color, icon, parent_id, is_group, excluded_from_expense_stats, sort_order)
SELECT 'EXPENSE', v.name, v.color, v.icon, p.id, false, p.excluded_from_expense_stats, v.sort_order
FROM (SELECT id, excluded_from_expense_stats FROM default_category_templates WHERE name = '매월 구독비용' AND parent_id IS NULL) p,
(VALUES
  ('카드 연회비', '#e03131', '🏠', 0),
  ('멤버십 비용', '#e03131', '💳', 1),
  ('경조사비', '#e03131', '🍾', 2),
  ('보영이네 계모임', '#e03131', '👪', 3),
  ('수민이네 계모임', '#e03131', '👪', 4)
) AS v(name, color, icon, sort_order);
