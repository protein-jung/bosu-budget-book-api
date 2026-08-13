ALTER TABLE assets
    ADD COLUMN loan_principal NUMERIC(16, 2),
    ADD COLUMN loan_start_month DATE,
    ADD COLUMN loan_term_months INTEGER,
    ADD COLUMN loan_monthly_payment NUMERIC(16, 2),
    ADD COLUMN loan_interest_rate NUMERIC(6, 3);
