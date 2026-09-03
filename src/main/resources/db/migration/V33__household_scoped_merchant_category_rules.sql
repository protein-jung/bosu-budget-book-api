-- merchant_category_rules를 전역 공유 테이블에서 가계부(household) 소유로 전환한다.
-- 1) 지금까지의 전역 규칙을 default_category_templates와 같은 패턴의 템플릿 테이블로 옮겨
--    새 가계부 생성 시 계속 기본값으로 쓸 수 있게 한다.
CREATE TABLE default_merchant_category_rule_templates (
    id BIGSERIAL PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL,
    color VARCHAR(20),
    icon VARCHAR(8),
    keywords_json TEXT NOT NULL,
    product_keywords_json TEXT NOT NULL DEFAULT '[]',
    sort_order INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

INSERT INTO default_merchant_category_rule_templates
    (category_name, color, icon, keywords_json, product_keywords_json, sort_order)
SELECT category_name, color, icon, keywords_json, product_keywords_json, sort_order
FROM merchant_category_rules
ORDER BY sort_order;

-- 2) merchant_category_rules를 household 소유로 바꾸고, 지금 있는 모든 가계부에 위 템플릿을
--    복제해 초기 규칙으로 채운다(당장은 모든 가계부가 같은 규칙을 갖되, 이후 가계부별로 각자
--    새 규칙을 추가/변경할 수 있다).
ALTER TABLE merchant_category_rules ADD COLUMN household_id BIGINT REFERENCES households(id) ON DELETE CASCADE;

INSERT INTO merchant_category_rules
    (household_id, category_name, color, icon, keywords_json, product_keywords_json, sort_order)
SELECT h.id, r.category_name, r.color, r.icon, r.keywords_json, r.product_keywords_json, r.sort_order
FROM households h
CROSS JOIN default_merchant_category_rule_templates r;

DELETE FROM merchant_category_rules WHERE household_id IS NULL;
ALTER TABLE merchant_category_rules ALTER COLUMN household_id SET NOT NULL;

DROP INDEX IF EXISTS idx_merchant_category_rules_sort_order;
CREATE INDEX idx_merchant_category_rules_household_id ON merchant_category_rules(household_id, sort_order);
