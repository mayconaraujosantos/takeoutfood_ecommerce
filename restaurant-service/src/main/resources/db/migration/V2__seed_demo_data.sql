-- Demo restaurant so the browse/menu/checkout flow has something to show.
-- owner_id is looked up by email against auth-service's seeded RESTAURANT_OWNER (see
-- auth-service's V3__seed_demo_users.sql) rather than hardcoded: restaurant-service and
-- auth-service share this physical database, but auth-service's users table accumulates
-- real signups before a demo seed is ever added, so that user's id isn't reliably 1 (see
-- V3's own comment on why it dropped explicit ids for the same reason). No explicit id or
-- ON CONFLICT here either, for the same reason -- existence is checked by name+owner instead.
INSERT INTO restaurants (name, description, cuisine_type, address, phone, owner_id, active, created_at, updated_at)
SELECT 'Pizza Napoli', 'Pizzaria tradicional italiana, forno a lenha.', 'Italiana', 'Rua das Flores, 123 - São Paulo, SP', '11999990000', u.id, TRUE, now(), now()
FROM users u
WHERE u.email = 'owner@ifood.local'
  AND NOT EXISTS (SELECT 1 FROM restaurants r WHERE r.name = 'Pizza Napoli' AND r.owner_id = u.id);
