-- V3's seed INSERT omitted account_locked/failed_login_attempts/phone_verified, expecting
-- them to fall back to a default -- but confirmed live, none of these columns actually have
-- a DB-level DEFAULT (information_schema.columns: column_default is empty for all three).
-- Hibernate's @Builder.Default only applies when a row is built through the Java entity
-- (normal /register flow), not to a row inserted by raw SQL, so V3's two demo users landed
-- with account_locked=NULL. User.isAccountNonLocked() does "!accountLocked", and unboxing a
-- null Boolean there throws a NullPointerException -- every login attempt against a user
-- seeded this way fails with a 401 masking that NPE ("Falha no login").
-- Scoped to any row with these still NULL, not just the two demo emails: the same gap would
-- bite any other row that ever gets raw-SQL-inserted the same way.
UPDATE users SET account_locked = FALSE WHERE account_locked IS NULL;
UPDATE users SET failed_login_attempts = 0 WHERE failed_login_attempts IS NULL;
UPDATE users SET phone_verified = FALSE WHERE phone_verified IS NULL;
