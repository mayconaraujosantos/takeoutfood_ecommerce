-- Demo users for exercising the checkout flow end to end. Password for both is
-- "password123" (BCrypt strength 12, matching SecurityConfig.passwordEncoder()).
--
-- No explicit ids here (unlike V1/V2's baseline data): this table accumulates real rows
-- from normal signups well before a demo seed ever gets added, so ids 1/2 can't be assumed
-- free -- ON CONFLICT (id) DO NOTHING would then silently skip inserting these two rows
-- entirely (wrong row already at that id, not a duplicate of this one), leaving the demo
-- accounts missing with no error. Matching by email is the actual uniqueness that matters.
INSERT INTO users (email, password, first_name, last_name, phone, role, active, email_verified, created_at, updated_at)
SELECT 'owner@ifood.local', '$2y$12$uhXt/sJ1.D6p2woqOOPkM.ovA8GZq6egR3OiTe70ddWJ4J3zX4ZLS', 'Restaurante', 'Dono', '11999990000', 'RESTAURANT_OWNER', TRUE, TRUE, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'owner@ifood.local');

INSERT INTO users (email, password, first_name, last_name, phone, role, active, email_verified, created_at, updated_at)
SELECT 'customer@ifood.local', '$2y$12$uhXt/sJ1.D6p2woqOOPkM.ovA8GZq6egR3OiTe70ddWJ4J3zX4ZLS', 'Cliente', 'Teste', '11988880000', 'CUSTOMER', TRUE, TRUE, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'customer@ifood.local');
