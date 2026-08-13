CREATE TABLE asset_snapshots (
    id BIGSERIAL PRIMARY KEY,
    household_id BIGINT NOT NULL REFERENCES households(id) ON DELETE CASCADE,
    snapshot_date DATE NOT NULL,
    total_value NUMERIC(16,2) NOT NULL,
    by_type_json TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (household_id, snapshot_date)
);

CREATE INDEX idx_asset_snapshots_household_date ON asset_snapshots(household_id, snapshot_date);
