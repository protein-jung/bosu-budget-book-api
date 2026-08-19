ALTER TABLE categories ADD COLUMN excluded_from_expense_stats BOOLEAN NOT NULL DEFAULT false;

UPDATE categories SET excluded_from_expense_stats = true
WHERE name = '미래준비'
   OR parent_id IN (SELECT id FROM categories WHERE name = '미래준비');
