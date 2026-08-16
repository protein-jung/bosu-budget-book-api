ALTER TABLE assets ADD COLUMN real_estate_category VARCHAR(20);
ALTER TABLE assets ADD COLUMN monthly_rent NUMERIC(16,2);

UPDATE assets SET real_estate_category = 'OWNED' WHERE type = 'REAL_ESTATE';
