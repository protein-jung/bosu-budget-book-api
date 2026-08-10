CREATE TABLE assets (
    id               BIGSERIAL PRIMARY KEY,
    household_id     BIGINT NOT NULL REFERENCES households(id) ON DELETE CASCADE,
    type             VARCHAR(20) NOT NULL,
    name             VARCHAR(100) NOT NULL,
    custodian        VARCHAR(100),
    symbol           VARCHAR(20),
    quantity         NUMERIC(20, 8),
    manual_value     NUMERIC(16, 2),
    current_price    NUMERIC(20, 4),
    price_updated_at TIMESTAMP,
    memo             VARCHAR(500),
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_assets_household_id ON assets(household_id);
