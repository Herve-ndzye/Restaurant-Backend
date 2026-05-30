# Authentication & Authorization Guide

## Overview

The Restaurant Order System now includes JWT-based authentication and role-based access control (RBAC) for four user types:

1. **CUSTOMER** - End users ordering food
2. **RESTAURANT_ADMIN** - Manages restaurant & menu
3. **KITCHEN_STAFF** - Processes incoming orders
4. **DELIVERY_DRIVER** - Delivers orders

## Authentication Endpoints

### Register a New User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "password": "securePassword123",
  "email": "john@example.com",
  "role": "CUSTOMER",
  "customerId": null,
  "restaurantId": null
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "role": "CUSTOMER"
}
```

### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "securePassword123"
}
```

**Response:** Same as registration

## Test Users

All test users have the password: **`password123`**

| Username | Role | Email | Restaurant |
|----------|------|-------|------------|
| customer1 | CUSTOMER | customer1@example.com | - |
| customer2 | CUSTOMER | customer2@example.com | - |
| admin_bella | RESTAURANT_ADMIN | admin@bellaitalia.com | Bella Italia (ID: 1) |
| admin_dragon | RESTAURANT_ADMIN | admin@dragonwok.com | Dragon Wok (ID: 2) |
| kitchen_bella | KITCHEN_STAFF | kitchen@bellaitalia.com | Bella Italia (ID: 1) |
| kitchen_dragon | KITCHEN_STAFF | kitchen@dragonwok.com | Dragon Wok (ID: 2) |
| driver1 | DELIVERY_DRIVER | driver1@example.com | - |

## Using JWT Tokens

After login/registration, include the token in all subsequent requests:

```http
GET /api/orders/1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Access Control Matrix

| Endpoint | CUSTOMER | RESTAURANT_ADMIN | KITCHEN_STAFF | DELIVERY_DRIVER |
|----------|----------|------------------|---------------|-----------------|
| **Public Endpoints** |
| GET /api/restaurants/** | ✅ | ✅ | ✅ | ✅ |
| POST /api/auth/register | ✅ | ✅ | ✅ | ✅ |
| POST /api/auth/login | ✅ | ✅ | ✅ | ✅ |
| **Customer Endpoints** |
| POST /api/customer/register | ✅ | ❌ | ❌ | ❌ |
| GET /api/customer/{id} | ✅ | ❌ | ❌ | ❌ |
| PUT /api/customer/{id} | ✅ | ❌ | ❌ | ❌ |
| POST /api/orders | ✅ | ❌ | ❌ | ❌ |
| GET /api/orders/customer/{id} | ✅ | ❌ | ❌ | ❌ |
| DELETE /api/orders/{id} | ✅ | ❌ | ❌ | ❌ |
| **Restaurant Admin Endpoints** |
| POST /api/restaurants/{id}/menu | ❌ | ✅ | ❌ | ❌ |
| PUT /api/restaurants/menu/{id} | ❌ | ✅ | ❌ | ❌ |
| DELETE /api/restaurants/menu/{id} | ❌ | ✅ | ❌ | ❌ |
| **Kitchen Staff Endpoints** |
| GET /api/kitchen/orders | ❌ | ❌ | ✅ | ❌ |
| PUT /api/kitchen/orders/{id}/accept | ❌ | ❌ | ✅ | ❌ |
| PUT /api/kitchen/orders/{id}/ready | ❌ | ❌ | ✅ | ❌ |
| PUT /api/kitchen/orders/{id}/reject | ❌ | ❌ | ✅ | ❌ |
| **Delivery Driver Endpoints** |
| PUT /api/orders/{id}/picked-up | ❌ | ❌ | ❌ | ✅ |
| PUT /api/orders/{id}/delivered | ❌ | ❌ | ❌ | ✅ |

## Example Workflows

### 1. Customer Places an Order

```bash
# 1. Login as customer
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"customer1","password":"password123"}'

# Save the token from response

# 2. Browse restaurants (no auth needed)
curl http://localhost:8080/api/restaurants

# 3. Place order (with auth)
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "restaurantId": 1,
    "items": [
      {"menuItemId": 1, "quantity": 2},
      {"menuItemId": 4, "quantity": 1}
    ]
  }'
```

### 2. Kitchen Staff Processes Order

```bash
# 1. Login as kitchen staff
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"kitchen_bella","password":"password123"}'

# 2. View pending orders
curl http://localhost:8080/api/kitchen/orders?restaurantId=1 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"

# 3. Accept order
curl -X PUT http://localhost:8080/api/kitchen/orders/1/accept \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"

# 4. Mark as ready
curl -X PUT http://localhost:8080/api/kitchen/orders/1/ready \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 3. Delivery Driver Delivers Order

```bash
# 1. Login as driver
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"driver1","password":"password123"}'

# 2. Pick up order
curl -X PUT http://localhost:8080/api/orders/1/picked-up \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"

# 3. Deliver order
curl -X PUT http://localhost:8080/api/orders/1/delivered \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 4. Restaurant Admin Manages Menu

```bash
# 1. Login as restaurant admin
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin_bella","password":"password123"}'

# 2. Add menu item
curl -X POST http://localhost:8080/api/restaurants/1/menu \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Fettuccine Alfredo",
    "price": 13.99,
    "category": "MAIN",
    "description": "Creamy pasta with parmesan"
  }'

# 3. Update menu item
curl -X PUT http://localhost:8080/api/restaurants/menu/1 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{"price": 14.99}'
```

## Security Features

✅ **JWT Token-based Authentication** - Stateless, scalable authentication
✅ **BCrypt Password Hashing** - Secure password storage
✅ **Role-Based Access Control (RBAC)** - Fine-grained permissions
✅ **Token Expiration** - 24-hour token validity
✅ **Stateless Sessions** - No server-side session storage
✅ **Protected Endpoints** - Authorization checks on all sensitive operations

## Error Responses

### 401 Unauthorized
```json
{
  "error": "Invalid username or password"
}
```

### 403 Forbidden
```json
{
  "error": "Access Denied"
}
```

### 400 Bad Request
```json
{
  "error": "Username already exists"
}
```

## Notes

- Tokens expire after 24 hours
- All passwords are hashed using BCrypt
- Public endpoints (browsing restaurants/menus) don't require authentication
- Each role has specific permissions based on their responsibilities
- Restaurant admins and kitchen staff are linked to specific restaurants
