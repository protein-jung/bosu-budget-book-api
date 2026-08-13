ALTER TABLE assets
    ADD COLUMN cash_category VARCHAR(20),
    ADD COLUMN maturity_date DATE;

UPDATE assets SET cash_category = 'ACCOUNT' WHERE type = 'CASH';
