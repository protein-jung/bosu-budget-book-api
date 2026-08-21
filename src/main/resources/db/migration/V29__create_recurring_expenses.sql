CREATE TABLE recurring_expenses (
    id BIGSERIAL PRIMARY KEY,
    household_id BIGINT NOT NULL REFERENCES households(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES categories(id),
    created_by_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    name VARCHAR(50) NOT NULL,
    amount NUMERIC(14,2) NOT NULL,
    day_of_month INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_generated_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_recurring_expenses_household_id ON recurring_expenses(household_id);
