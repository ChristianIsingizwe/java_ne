ALTER TABLE notifications
DROP CONSTRAINT IF EXISTS chk_notifications_type;

ALTER TABLE notifications
ADD CONSTRAINT chk_notifications_type
CHECK (notification_type IN ('BILL_GENERATED', 'PARTIAL_PAYMENT_RECEIVED', 'PAYMENT_COMPLETED'));

CREATE OR REPLACE FUNCTION apply_payment_approval()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    approved_total NUMERIC(14, 2);
    bill_total NUMERIC(14, 2);
    remaining_balance NUMERIC(14, 2);
    bill_year INTEGER;
    bill_month INTEGER;
    customer_id_ref BIGINT;
    customer_name TEXT;
    bill_period TEXT;
BEGIN
    IF NEW.status <> 'APPROVED' OR (TG_OP = 'UPDATE' AND OLD.status = 'APPROVED') THEN
        RETURN NEW;
    END IF;

    SELECT COALESCE(SUM(amount_paid), 0.00)
      INTO approved_total
      FROM payments
     WHERE bill_id = NEW.bill_id
       AND status = 'APPROVED';

    SELECT total_amount, billing_year, billing_month, customer_id
      INTO bill_total, bill_year, bill_month, customer_id_ref
      FROM bills
     WHERE id = NEW.bill_id;

    remaining_balance := GREATEST(bill_total - approved_total, 0.00);

    UPDATE bills
       SET amount_paid = approved_total,
           outstanding_balance = remaining_balance,
           status = CASE
               WHEN approved_total >= total_amount THEN 'PAID'
               WHEN approved_total > 0 THEN 'PARTIALLY_PAID'
               ELSE status
           END
     WHERE id = NEW.bill_id;

    SELECT p.full_name
      INTO customer_name
      FROM customers c
      JOIN profiles p ON p.id = c.profile_id
     WHERE c.id = customer_id_ref;

    bill_period := to_char(make_date(bill_year, bill_month, 1), 'FMMonth') || ' / ' || bill_year;

    IF approved_total >= bill_total THEN
        INSERT INTO notifications (customer_id, bill_id, payment_id, notification_type, message, status)
        VALUES (
            customer_id_ref,
            NEW.bill_id,
            NEW.id,
            'PAYMENT_COMPLETED',
            'Dear ' || customer_name || E'.\nYour ' || bill_period || ' utility bill of ' || bill_total || ' FRW has been fully paid.',
            'PENDING'
        );
    ELSE
        INSERT INTO notifications (customer_id, bill_id, payment_id, notification_type, message, status)
        VALUES (
            customer_id_ref,
            NEW.bill_id,
            NEW.id,
            'PARTIAL_PAYMENT_RECEIVED',
            'Dear ' || customer_name || E'.\nWe have received your partial payment of ' || NEW.amount_paid
                || ' FRW for the ' || bill_period || ' utility bill. Remaining balance: ' || remaining_balance || ' FRW.',
            'PENDING'
        );
    END IF;

    RETURN NEW;
END;
$$;
