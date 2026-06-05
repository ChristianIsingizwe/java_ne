UPDATE meters
SET billing_mode = 'POSTPAID'
WHERE billing_mode = 'PREPAID';

ALTER TABLE meters DROP CONSTRAINT IF EXISTS chk_water_postpaid;
ALTER TABLE meters DROP CONSTRAINT IF EXISTS chk_meters_billing_mode;

ALTER TABLE meters
    ADD CONSTRAINT chk_meters_billing_mode CHECK (billing_mode = 'POSTPAID');
