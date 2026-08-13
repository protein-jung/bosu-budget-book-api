ALTER TABLE categories
    ADD COLUMN is_group BOOLEAN NOT NULL DEFAULT false;

-- 이미 하위 카테고리를 가진 상위 카테고리는 그룹으로 표시해둔다.
UPDATE categories
SET is_group = true
WHERE id IN (SELECT DISTINCT parent_id FROM categories WHERE parent_id IS NOT NULL);
