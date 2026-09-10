-- The orders table on already-deployed databases (created by Hibernate ddl-auto before
-- Flyway existed) has a status CHECK constraint that predates OUT_FOR_DELIVERY and
-- DELIVERED (confirmed live: pg_get_constraintdef only lists CART..CANCELLED minus those
-- two). baseline-on-migrate skips V1 there, so V1's up-to-date constraint never applies --
-- this has to run as its own migration to actually reach those databases.
-- Safe to run on a brand-new database too: V1 already created the constraint with the full
-- set, so this DROP/ADD is a no-op there (same definition, just re-asserted).
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_status_check;
ALTER TABLE orders ADD CONSTRAINT orders_status_check
    CHECK (status IN ('CART', 'PENDING_PAYMENT', 'CONFIRMED', 'PAYMENT_FAILED', 'PREPARING', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED'));
