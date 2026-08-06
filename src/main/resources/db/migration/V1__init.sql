CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    name          VARCHAR(100) NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE households (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    invite_code   VARCHAR(20) NOT NULL UNIQUE,
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE household_members (
    id            BIGSERIAL PRIMARY KEY,
    household_id  BIGINT NOT NULL REFERENCES households(id) ON DELETE CASCADE,
    user_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role          VARCHAR(20) NOT NULL,
    joined_at     TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (household_id, user_id)
);

CREATE INDEX idx_household_members_user_id ON household_members(user_id);

CREATE TABLE categories (
    id            BIGSERIAL PRIMARY KEY,
    household_id  BIGINT NOT NULL REFERENCES households(id) ON DELETE CASCADE,
    name          VARCHAR(50) NOT NULL,
    type          VARCHAR(20) NOT NULL,
    color         VARCHAR(20),
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_categories_household_id ON categories(household_id);

CREATE TABLE cards (
    id            BIGSERIAL PRIMARY KEY,
    household_id  BIGINT NOT NULL REFERENCES households(id) ON DELETE CASCADE,
    name          VARCHAR(50) NOT NULL,
    type          VARCHAR(20) NOT NULL,
    owner_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_cards_household_id ON cards(household_id);

CREATE TABLE transactions (
    id                BIGSERIAL PRIMARY KEY,
    household_id      BIGINT NOT NULL REFERENCES households(id) ON DELETE CASCADE,
    type              VARCHAR(20) NOT NULL,
    amount            NUMERIC(14, 2) NOT NULL,
    transaction_date  DATE NOT NULL,
    category_id       BIGINT NOT NULL REFERENCES categories(id),
    card_id           BIGINT REFERENCES cards(id) ON DELETE SET NULL,
    user_id           BIGINT NOT NULL REFERENCES users(id),
    memo              VARCHAR(500),
    created_at        TIMESTAMP NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_transactions_household_date ON transactions(household_id, transaction_date);
