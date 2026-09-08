-- Demo menu items for the seeded "Pizza Napoli" restaurant (see restaurant-service's
-- V2__seed_demo_data.sql). restaurant_id is looked up by name rather than hardcoded, for the
-- same reason that seed no longer hardcodes its own id -- see its comment.
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
