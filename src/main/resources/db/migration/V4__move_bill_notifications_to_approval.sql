DELETE FROM notifications n
USING bills b
WHERE n.bill_id = b.id
  AND n.notification_type = 'BILL_GENERATED'
  AND b.status = 'PENDING_APPROVAL';

DROP TRIGGER IF EXISTS trg_bill_notification ON bills;

CREATE OR REPLACE FUNCTION insert_bill_notification()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    customer_name TEXT;
    bill_period TEXT;
BEGIN
    IF NEW.status <> 'APPROVED' OR (TG_OP = 'UPDATE' AND OLD.status = 'APPROVED') THEN
        RETURN NEW;
    END IF;

    IF EXISTS (
        SELECT 1
          FROM notifications
         WHERE bill_id = NEW.id
           AND notification_type = 'BILL_GENERATED'
    ) THEN
        RETURN NEW;
    END IF;

    SELECT p.full_name
      INTO customer_name
      FROM customers c
      JOIN profiles p ON p.id = c.profile_id
     WHERE c.id = NEW.customer_id;

    bill_period := to_char(make_date(NEW.billing_year, NEW.billing_month, 1), 'FMMonth') || ' / ' || NEW.billing_year;

    INSERT INTO notifications (customer_id, bill_id, notification_type, message, status)
    VALUES (
        NEW.customer_id,
        NEW.id,
        'BILL_GENERATED',
        'Dear ' || customer_name || E'.\nYour ' || bill_period || ' utility bill of ' || NEW.total_amount || ' FRW has been successfully processed.',
        'PENDING'
    );
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_bill_notification
AFTER UPDATE OF status ON bills
FOR EACH ROW EXECUTE FUNCTION insert_bill_notification();
