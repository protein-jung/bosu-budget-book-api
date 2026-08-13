ALTER TABLE assets ADD COLUMN loan_repayment_type VARCHAR(20);
UPDATE assets SET loan_repayment_type = 'EQUAL_INSTALLMENT' WHERE type = 'LOAN';
