ALTER TABLE transactions
    ADD COLUMN is_backfill BOOLEAN NOT NULL DEFAULT false;
