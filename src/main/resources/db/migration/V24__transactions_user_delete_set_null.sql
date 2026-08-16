ALTER TABLE transactions ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE transactions DROP CONSTRAINT transactions_user_id_fkey;
ALTER TABLE transactions ADD CONSTRAINT transactions_user_id_fkey
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;
