-- Insert Restaurants
INSERT INTO Restaurant (name, cuisine, address, is_open) VALUES 
('Bella Italia', 'Italian', '123 Main Street, Downtown, City Center', TRUE),
('Dragon Wok', 'Chinese', '456 East Avenue, Chinatown District, Metro City', TRUE);

-- Bella Italia Menu (Restaurant ID = 1)
INSERT INTO MenuItem (restaurant, name, description, price, category, is_available) VALUES 
-- Starters
(1, 'Bruschetta', 'Toasted bread topped with fresh tomatoes, garlic, basil, and olive oil', 6.99, 'STARTER', TRUE),
(1, 'Caprese Salad', 'Fresh mozzarella, tomatoes, and basil drizzled with balsamic glaze', 8.99, 'STARTER', TRUE),
(1, 'Garlic Bread', 'Crispy Italian bread with garlic butter and herbs', 4.50, 'STARTER', TRUE),
-- Main Courses
(1, 'Margherita Pizza', 'Classic pizza with tomato sauce, mozzarella, and fresh basil', 12.99, 'MAIN', TRUE),
(1, 'Spaghetti Carbonara', 'Creamy pasta with bacon, eggs, parmesan, and black pepper', 14.99, 'MAIN', TRUE),
(1, 'Chicken Parmigiana', 'Breaded chicken breast with marinara sauce and melted mozzarella', 16.99, 'MAIN', TRUE),
(1, 'Lasagna Bolognese', 'Layers of pasta with meat sauce, béchamel, and cheese', 15.99, 'MAIN', TRUE),
-- Desserts
(1, 'Tiramisu', 'Classic Italian dessert with coffee-soaked ladyfingers and mascarpone', 7.50, 'DESSERT', TRUE),
(1, 'Panna Cotta', 'Creamy vanilla custard with berry compote', 6.50, 'DESSERT', TRUE),
-- Drinks
(1, 'Italian Soda', 'Sparkling water with flavored syrup', 3.99, 'DRINK', TRUE),
(1, 'Espresso', 'Strong Italian coffee', 2.99, 'DRINK', TRUE),
(1, 'Limoncello Spritz', 'Refreshing lemon cocktail with prosecco', 8.99, 'DRINK', TRUE),
-- Sides
(1, 'Caesar Salad', 'Romaine lettuce with parmesan, croutons, and Caesar dressing', 5.99, 'SIDE', TRUE),
(1, 'Roasted Vegetables', 'Seasonal vegetables roasted with olive oil and herbs', 4.99, 'SIDE', TRUE);

-- Dragon Wok Menu (Restaurant ID = 2)
INSERT INTO MenuItem (restaurant, name, description, price, category, is_available) VALUES 
-- Starters
(2, 'Spring Rolls', 'Crispy vegetable spring rolls with sweet chili sauce', 5.99, 'STARTER', TRUE),
(2, 'Dumplings', 'Steamed pork and vegetable dumplings with soy dipping sauce', 7.99, 'STARTER', TRUE),
(2, 'Hot and Sour Soup', 'Spicy and tangy soup with tofu, mushrooms, and bamboo shoots', 4.99, 'STARTER', TRUE),
(2, 'Crispy Wontons', 'Deep-fried wontons filled with cream cheese and crab', 6.99, 'STARTER', TRUE),
-- Main Courses
(2, 'Kung Pao Chicken', 'Spicy stir-fried chicken with peanuts, vegetables, and chili peppers', 13.99, 'MAIN', TRUE),
(2, 'Sweet and Sour Pork', 'Crispy pork with bell peppers and pineapple in sweet and sour sauce', 14.99, 'MAIN', TRUE),
(2, 'Beef with Broccoli', 'Tender beef slices with broccoli in savory brown sauce', 15.99, 'MAIN', TRUE),
(2, 'General Tso Chicken', 'Crispy chicken in sweet and spicy sauce', 13.99, 'MAIN', TRUE),
(2, 'Shrimp Fried Rice', 'Wok-fried rice with shrimp, eggs, and vegetables', 12.99, 'MAIN', TRUE),
(2, 'Chow Mein', 'Stir-fried noodles with vegetables and choice of protein', 11.99, 'MAIN', TRUE),
-- Desserts
(2, 'Mango Pudding', 'Smooth and creamy mango dessert', 4.99, 'DESSERT', TRUE),
(2, 'Fried Ice Cream', 'Vanilla ice cream in crispy coating with honey drizzle', 5.99, 'DESSERT', TRUE),
(2, 'Fortune Cookies', 'Traditional crispy cookies with fortune messages', 2.99, 'DESSERT', TRUE),
-- Drinks
(2, 'Jasmine Tea', 'Traditional Chinese green tea with jasmine flowers', 2.99, 'DRINK', TRUE),
(2, 'Bubble Tea', 'Sweet milk tea with tapioca pearls', 4.99, 'DRINK', TRUE),
(2, 'Lychee Juice', 'Refreshing tropical lychee drink', 3.99, 'DRINK', TRUE),
-- Sides
(2, 'Steamed Rice', 'Fluffy white jasmine rice', 2.99, 'SIDE', TRUE),
(2, 'Egg Fried Rice', 'Fried rice with scrambled eggs and green onions', 4.99, 'SIDE', TRUE),
(2, 'Stir-Fried Vegetables', 'Mixed vegetables in garlic sauce', 5.99, 'SIDE', TRUE);
