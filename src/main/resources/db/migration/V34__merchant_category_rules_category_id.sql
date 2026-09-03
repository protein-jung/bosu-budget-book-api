-- merchant_category_rules가 카테고리를 이름(문자열)이 아니라 실제 categories 행을 참조하도록
-- 바꾼다. 이름 문자열 매칭은 사용자가 카테고리 이름을 바꾸면 조용히 어긋나는 문제가 있었다
-- (실제로 여러 가계부에서 카테고리명이 바뀌어 상당수 규칙이 이미 아무 것도 매칭하지 못하고
-- 있었다). category_id를 쓰면 규칙 수정 화면에서도 지금 어떤 카테고리가 연결돼 있는지 명확하다.
ALTER TABLE merchant_category_rules ADD COLUMN category_id BIGINT REFERENCES categories(id) ON DELETE CASCADE;

UPDATE merchant_category_rules r
SET category_id = c.id
FROM categories c
WHERE c.household_id = r.household_id
  AND c.name = r.category_name
  AND c.type = 'EXPENSE';

-- 카테고리명이 실제 카테고리와 안 맞아 이미 아무 역할도 못 하던 규칙은 정리한다.
DELETE FROM merchant_category_rules WHERE category_id IS NULL;

ALTER TABLE merchant_category_rules ALTER COLUMN category_id SET NOT NULL;
ALTER TABLE merchant_category_rules DROP COLUMN category_name;
ALTER TABLE merchant_category_rules DROP COLUMN color;
ALTER TABLE merchant_category_rules DROP COLUMN icon;

CREATE INDEX idx_merchant_category_rules_category_id ON merchant_category_rules(category_id);
