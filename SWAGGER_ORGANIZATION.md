# Swagger UI Organization

## Clean Structure

The API is now organized into **6 well-numbered sections** with no duplicates:

### 1. Authentication
**Description:** Registration and login for all user roles  
**Endpoints:**
- `POST /api/auth/register` - Register a new user (generic)
- `POST /api/auth/login` - Login to get JWT token
- `POST /api/auth/register/customer` - Register a new customer
- `POST /api/auth/register/kitchen-staff` - Register a new kitchen staff member
- `POST /api/auth/register/delivery-driver` - Register a new delivery driver
- `POST /api/auth/register/admin` - Register a new admin (requires invite code)
- `PUT /api/auth/change-password` - Change password

**Controller:** `AuthController.java`

---

### 2. Customer
**Description:** Customer profile management and order operations  
**Endpoints:**

**Profile Management:**
- `GET /api/customer/{id}` - Get customer profile
- `PUT /api/customer/{id}` - Update customer profile
- `PUT /api/customer/{id}/deactivate` - Deactivate customer account
- `DELETE /api/customer/{id}` - Delete customer profile

**Order Operations:**
- `POST /api/orders` - Place a new order
- `GET /api/orders/{id}` - Get order by ID
- `GET /api/orders/customer/{customerId}` - Get customer orders
- `DELETE /api/orders/{id}` - Cancel order
- `GET /api/orders/{id}/status` - Get order status

**Controllers:** 
- `CustomerController.java` - Profile management
- `OrderController.java` - Order operations

---

### 3. Kitchen Staff
**Description:** Kitchen order management and preparation workflow  
**Endpoints:**
- `GET /api/kitchen/orders` - Get pending orders
- `PUT /api/kitchen/orders/{id}/accept` - Accept order
- `PUT /api/kitchen/orders/{id}/ready` - Mark order as ready
- `PUT /api/kitchen/orders/{id}/reject` - Reject order

**Controller:** `KitchenController.java`

---

### 4. Delivery Driver
**Description:** Order pickup and delivery tracking  
**Endpoints:**
- `PUT /api/delivery/orders/{id}/picked-up` - Mark order as picked up
- `PUT /api/delivery/orders/{id}/delivered` - Mark order as delivered

**Controller:** `DeliveryController.java`

---

### 5. Admin
**Description:** Customer management, menu management, and admin invitations  
**Endpoints:**

**Customer Management:**
- `GET /api/admin/customers` - Get all customers (paginated)

**Admin Management:**
- `POST /api/admin/create-admin` - Create admin invitation

**Menu Management:**
- `POST /api/restaurants/{id}/menu` - Add menu item
- `PUT /api/restaurants/menu/{id}` - Update menu item
- `DELETE /api/restaurants/menu/{id}` - Delete menu item

**Controllers:**
- `CustomerAdminController.java` - Customer & admin management
- `RestaurantController.java` - Menu management

---

### 6. Public - Restaurants
**Description:** Browse restaurants and menus (public access)  
**Endpoints:**
- `GET /api/restaurants` - Get all restaurants
- `GET /api/restaurants/{id}` - Get restaurant by ID
- `GET /api/restaurants/{id}/menu` - Get restaurant menu

**Controller:** `RestaurantController.java`

---

## Implementation Details

### How Tags Work

Each controller has **ONE** class-level `@Tag` annotation:

```java
@RestController
@RequestMapping("/api/auth")
@Tag(name = "1. Authentication", description = "Registration and login for all user roles")
public class AuthController {
    // All methods inherit this tag
}
```

### Why This Organization?

1. **Numbered (1-6):** Clear ordering in Swagger UI
2. **Role-based:** Grouped by who uses the endpoints
3. **No duplicates:** Each endpoint appears in only one section
4. **Clean descriptions:** Short, clear purpose statements
5. **Logical flow:** Authentication → Customer → Staff → Admin → Public

### Special Cases

**RestaurantController** serves two purposes:
- **Public endpoints** (GET operations) → Tag: "6. Public - Restaurants"
- **Admin endpoints** (POST/PUT/DELETE menu) → Appear in "6. Public - Restaurants" but require RESTAURANT_ADMIN role

This is intentional because:
- Menu items are restaurant resources
- Grouping all restaurant-related operations together makes sense
- Security is enforced by annotations, not Swagger organization

## Verification

After restarting the application, you should see **ONLY** these 6 sections in Swagger UI:

✅ 1. Authentication  
✅ 2. Customer  
✅ 3. Kitchen Staff  
✅ 4. Delivery Driver  
✅ 5. Admin  
✅ 6. Public - Restaurants  

❌ No "Authentication" (without number)  
❌ No "Customer Orders"  
❌ No "Customer Profile"  
❌ No "Kitchen Operations"  
❌ No "Delivery Operations"  
❌ No "Menu Management"  
❌ No "Restaurants"  

## Access Swagger UI

```
http://localhost:8080/swagger-ui.html
```

Or

```
http://localhost:8080/swagger-ui/index.html
```

## Troubleshooting

If you still see duplicate sections:

1. **Clear browser cache:** Ctrl+Shift+Delete or Cmd+Shift+Delete
2. **Hard refresh:** Ctrl+F5 or Cmd+Shift+R
3. **Restart application:** `./mvnw spring-boot:run`
4. **Check for caching:** Open in incognito/private window

## Files Modified

- ✅ `AuthController.java` - Cleaned up tag
- ✅ `CustomerController.java` - Cleaned up tag
- ✅ `CustomerAdminController.java` - Cleaned up tag
- ✅ `OrderController.java` - Removed method-level tags, added class-level tag
- ✅ `KitchenController.java` - Cleaned up tag
- ✅ `DeliveryController.java` - Cleaned up tag
- ✅ `RestaurantController.java` - Removed method-level tags, added class-level tag

All method-level `@Tag` annotations have been removed. Each controller now has exactly ONE class-level `@Tag` annotation.
