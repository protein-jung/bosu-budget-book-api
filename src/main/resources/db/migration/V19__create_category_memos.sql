CREATE TABLE category_memos (
    id           BIGSERIAL PRIMARY KEY,
    category_id  BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    year         INT NOT NULL,
    month        INT NOT NULL,
    memo         VARCHAR(500) NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (category_id, year, month)
);
