CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role ENUM('CUSTOMER', 'RESTAURANT_ADMIN', 'KITCHEN_STAFF', 'DELIVERY_DRIVER') NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    customer_id BIGINT NULL,
    restaurant_id BIGINT NULL,
    CONSTRAINT fk_user_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
    CONSTRAINT fk_user_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id)
);

-- Create index for faster lookups
CREATE INDEX idx_user_username ON user(username);
CREATE INDEX idx_user_email ON user(email);
CREATE INDEX idx_user_role ON user(role);
