CREATE TABLE import_batches (
    id              BIGSERIAL PRIMARY KEY,
    household_id    BIGINT NOT NULL REFERENCES households(id) ON DELETE CASCADE,
    provider        VARCHAR(30) NOT NULL,
    card_id         BIGINT REFERENCES cards(id) ON DELETE SET NULL,
    file_checksum   VARCHAR(64) NOT NULL,
    imported_count  INTEGER NOT NULL,
    skipped_count   INTEGER NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (household_id, file_checksum)
);
