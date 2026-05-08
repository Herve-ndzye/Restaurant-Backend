CREATE TABLE Customer
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(100) NOT NULL,
    phone     VARCHAR(20)  NOT NULL,
    address   TEXT         NOT NULL,
    created_at TIMESTAMP   default (curdate())  NOT NULL
);

CREATE TABLE Restaurant
(
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    name    VARCHAR(100)         NOT NULL,
    cuisine VARCHAR(50)          NOT NULL,
    address TEXT                 NOT NULL,
    isOpen  BOOLEAN DEFAULT TRUE NULL
);

CREATE TABLE MenuItem
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    restaurant  BIGINT                                               NULL,
    name        VARCHAR(100)                                         NOT NULL,
    description TEXT                                                 NULL,
    price       DECIMAL(10, 2) CHECK (price > 0)                    NULL,
    category    ENUM ('STARTER', 'MAIN', 'DESSERT', 'DRINK', 'SIDE') NOT NULL,
    isAvailable BOOLEAN DEFAULT TRUE                                 NULL,
    CONSTRAINT MenuItem_restaurant_id_fk
        FOREIGN KEY (restaurant) REFERENCES Restaurant (id)
);

CREATE TABLE `order`
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer        BIGINT                                                              NOT NULL,
    restaurant      BIGINT                                                              NOT NULL,
    status          ENUM ('PENDING','ACCEPTED', 'PREPARING','READY','PICKED_UP', 'DELIVERED', 'CANCELLED','REJECTED') NOT NULL,
    totalPrice      DECIMAL(10, 2)                                                      NOT NULL,
    rejectionReason TEXT                                                                NULL,
    createAt        TIMESTAMP                                                           NOT NULL,
    updateAt        TIMESTAMP                                                           NOT NULL,
    CONSTRAINT order_customer_id_fk
        FOREIGN KEY (customer) REFERENCES Customer (id),
    CONSTRAINT order_restaurant_id_fk
        FOREIGN KEY (restaurant) REFERENCES Restaurant (id)
);

CREATE TABLE OrderItem
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order`  BIGINT                           NOT NULL,
    menuItem BIGINT                           NULL,
    quantity INT CHECK (quantity >= 1)        NOT NULL,
    subtotal DECIMAL(10, 2)                   NOT NULL,
    CONSTRAINT OrderItem_MenuItem_id_fk
        FOREIGN KEY (menuItem) REFERENCES MenuItem (id),
    CONSTRAINT OrderItem_order_id_fk
        FOREIGN KEY (`order`) REFERENCES `order` (id)
);