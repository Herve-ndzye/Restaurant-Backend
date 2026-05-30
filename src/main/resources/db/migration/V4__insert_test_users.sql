-- Insert test users with BCrypt encoded password "password123"
-- BCrypt hash for "password123": $2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG

-- Customer user (linked to customer ID 1 if exists)
INSERT INTO user (username, password, email, role, is_active, customer_id, restaurant_id) 
VALUES ('customer1', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'customer1@example.com', 'CUSTOMER', TRUE, NULL, NULL);

-- Restaurant Admin user (linked to restaurant ID 1 - Bella Italia)
INSERT INTO user (username, password, email, role, is_active, customer_id, restaurant_id) 
VALUES ('admin_bella', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'admin@bellaitalia.com', 'RESTAURANT_ADMIN', TRUE, NULL, 1);

-- Restaurant Admin user (linked to restaurant ID 2 - Dragon Wok)
INSERT INTO user (username, password, email, role, is_active, customer_id, restaurant_id) 
VALUES ('admin_dragon', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'admin@dragonwok.com', 'RESTAURANT_ADMIN', TRUE, NULL, 2);

-- Kitchen Staff user (for Bella Italia)
INSERT INTO user (username, password, email, role, is_active, customer_id, restaurant_id) 
VALUES ('kitchen_bella', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'kitchen@bellaitalia.com', 'KITCHEN_STAFF', TRUE, NULL, 1);

-- Kitchen Staff user (for Dragon Wok)
INSERT INTO user (username, password, email, role, is_active, customer_id, restaurant_id) 
VALUES ('kitchen_dragon', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'kitchen@dragonwok.com', 'KITCHEN_STAFF', TRUE, NULL, 2);

-- Delivery Driver user
INSERT INTO user (username, password, email, role, is_active, customer_id, restaurant_id) 
VALUES ('driver1', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'driver1@example.com', 'DELIVERY_DRIVER', TRUE, NULL, NULL);

-- Another customer
INSERT INTO user (username, password, email, role, is_active, customer_id, restaurant_id) 
VALUES ('customer2', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'customer2@example.com', 'CUSTOMER', TRUE, NULL, NULL);
