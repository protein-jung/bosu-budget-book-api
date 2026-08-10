ALTER TABLE categories
    ADD COLUMN target_amount NUMERIC(14, 2);

CREATE TABLE category_monthly_targets (
    id           BIGSERIAL PRIMARY KEY,
    category_id  BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    year         INT NOT NULL,
    month        INT NOT NULL,
    amount       NUMERIC(14, 2) NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (category_id, year, month)
);
