ALTER TABLE categories
    ADD COLUMN parent_id BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    ADD COLUMN sort_order INT NOT NULL DEFAULT 0;

CREATE INDEX idx_categories_parent_id ON categories(parent_id);
