ALTER TABLE fraud_result
    ADD COLUMN policy_version VARCHAR(100) NOT NULL DEFAULT 'legacy-unversioned';

ALTER TABLE fraud_result
    ADD COLUMN event_category VARCHAR(50) NOT NULL DEFAULT 'uncategorized';

ALTER TABLE fraud_result
    ADD COLUMN transaction_timestamp TIMESTAMP WITH TIME ZONE;
