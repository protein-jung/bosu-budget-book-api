-- 신규 가입자에게 특정 가계부(doslxk@gmail.com)의 개인화된 수입 항목(수민월급, 보영 월급,
-- 육아휴직급여 등)을 그대로 물려주지 않도록, 기본 수입 카테고리를 누구에게나 통하는
-- 월급/부수입/기타수입 세 가지로 단순화한다. 이미 가계부를 만든 사용자의 카테고리에는
-- 영향이 없다 — CategoryDefaultSeeder는 household 생성 시 한 번만 이 템플릿을 복제한다.
DELETE FROM default_category_templates WHERE type = 'INCOME';

INSERT INTO default_category_templates (type, name, color, icon, parent_id, is_group, excluded_from_expense_stats, sort_order) VALUES
('INCOME', '월급', '#e03131', '🏦', NULL, false, false, 0),
('INCOME', '부수입', '#e03131', '💵', NULL, false, false, 1),
('INCOME', '기타수입', '#e03131', '❓', NULL, false, false, 2);
