-- V2 ran and found no "Pizza Napoli" row yet (confirmed: flyway_schema_history_menu shows
-- V2 succeeded at 04:10:24.012, flyway_schema_history_restaurant shows restaurant-service's
-- own seed didn't insert it until 04:10:24.764 -- menu-service and restaurant-service start
-- independently in Kubernetes, so nothing guarantees which one's migrations run first).
-- V2's INSERT...SELECT...WHERE NOT EXISTS is silently a no-op when the SELECT matches
-- nothing, not an error, so this has to be a new migration -- Flyway won't re-run V2 now
-- that it's recorded as successful, empty result or not.
--
-- This retry is not a structural fix for the underlying race: it works here because
-- restaurant-service's seed has since completed, so this will find "Pizza Napoli" on this
-- and any later deploy. On a genuinely first-ever deploy of both services together, V2 and
-- this V3 both run back-to-back in the same startup, milliseconds apart -- if
-- restaurant-service is still slower than that, the race reproduces across both. Fixing
-- that for real needs actual cross-service startup ordering (e.g. an Argo sync wave or an
-- init container), out of scope for a demo seed.
INSERT INTO menu_items (restaurant_id, name, description, price, category, available, created_at, updated_at)
SELECT r.id, 'Pizza Margherita', 'Molho de tomate, mussarela e manjericão fresco.', 39.90, 'Pizzas', TRUE, now(), now()
FROM restaurants r
WHERE r.name = 'Pizza Napoli'
  AND NOT EXISTS (SELECT 1 FROM menu_items m WHERE m.restaurant_id = r.id AND m.name = 'Pizza Margherita');

INSERT INTO menu_items (restaurant_id, name, description, price, category, available, created_at, updated_at)
SELECT r.id, 'Pizza Calabresa', 'Molho de tomate, mussarela, calabresa e cebola.', 42.90, 'Pizzas', TRUE, now(), now()
FROM restaurants r
WHERE r.name = 'Pizza Napoli'
  AND NOT EXISTS (SELECT 1 FROM menu_items m WHERE m.restaurant_id = r.id AND m.name = 'Pizza Calabresa');

INSERT INTO menu_items (restaurant_id, name, description, price, category, available, created_at, updated_at)
SELECT r.id, 'Refrigerante Lata 350ml', 'Coca-Cola, Guaraná ou Fanta.', 6.00, 'Bebidas', TRUE, now(), now()
FROM restaurants r
WHERE r.name = 'Pizza Napoli'
  AND NOT EXISTS (SELECT 1 FROM menu_items m WHERE m.restaurant_id = r.id AND m.name = 'Refrigerante Lata 350ml');
