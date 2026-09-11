-- 신규 가입자에게 물려줄 기본 지출 카테고리를 특정 가계부의 세세한 구조 대신
-- 장보기/외식/주거비 세 대분류와 그 아래 최소한의 소분류만 남기도록 단순화한다.
-- parent_id는 default_category_templates(id) ON DELETE CASCADE라서 대분류를
-- 지우면 소분류도 함께 지워진다. 이미 가계부를 만든 사용자의 카테고리에는 영향이
-- 없다 — CategoryDefaultSeeder는 household 생성 시 한 번만 이 템플릿을 복제한다.
DELETE FROM default_category_templates WHERE type = 'EXPENSE';

INSERT INTO default_category_templates (type, name, color, icon, parent_id, is_group, excluded_from_expense_stats, sort_order) VALUES
('EXPENSE', '장보기', '#f08c00', '🛒', NULL, true, false, 0),
('EXPENSE', '외식', '#e03131', '🍽️', NULL, true, false, 1),
('EXPENSE', '주거비', '#0c8599', '🏠', NULL, true, false, 2);

INSERT INTO default_category_templates (type, name, color, icon, parent_id, is_group, excluded_from_expense_stats, sort_order)
SELECT 'EXPENSE', v.name, v.color, v.icon, p.id, false, false, v.sort_order
FROM (SELECT id FROM default_category_templates WHERE name = '장보기' AND type = 'EXPENSE' AND parent_id IS NULL) p,
(VALUES
  ('생필품', '#f08c00', '🧻', 0),
  ('식료품', '#2f9e44', '🥬', 1)
) AS v(name, color, icon, sort_order);

INSERT INTO default_category_templates (type, name, color, icon, parent_id, is_group, excluded_from_expense_stats, sort_order)
SELECT 'EXPENSE', v.name, v.color, v.icon, p.id, false, false, v.sort_order
FROM (SELECT id FROM default_category_templates WHERE name = '외식' AND type = 'EXPENSE' AND parent_id IS NULL) p,
(VALUES
  ('카페', '#7048e8', '☕', 0),
  ('배달', '#f08c00', '🛵', 1),
  ('음식점', '#e64980', '🍽️', 2)
) AS v(name, color, icon, sort_order);

INSERT INTO default_category_templates (type, name, color, icon, parent_id, is_group, excluded_from_expense_stats, sort_order)
SELECT 'EXPENSE', v.name, v.color, v.icon, p.id, false, false, v.sort_order
FROM (SELECT id FROM default_category_templates WHERE name = '주거비' AND type = 'EXPENSE' AND parent_id IS NULL) p,
(VALUES
  ('관리비', '#0c8599', '🏠', 0),
  ('인터넷', '#1971c2', '🌐', 1)
) AS v(name, color, icon, sort_order);
