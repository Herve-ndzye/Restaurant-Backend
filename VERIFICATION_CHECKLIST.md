# Verification Checklist

## After Restarting Application

### ✅ Step 1: Restart Application
```bash
./mvnw spring-boot:run
```

### ✅ Step 2: Check Swagger UI
Open: http://localhost:8080/swagger-ui.html

**Should See Exactly 6 Sections:**

1. **1. Authentication** (6 endpoints)
   - ✅ Login
   - ✅ Register Customer
   - ✅ Register Kitchen Staff
   - ✅ Register Delivery Driver
   - ✅ Register Admin
   - ✅ Change Password
   - ❌ ~~Register (generic)~~ - REMOVED

2. **2. Customer** (8 endpoints)
   - ✅ Get Profile
   - ✅ Update Profile
   - ✅ Deactivate Account
   - ✅ Place Order
   - ✅ Get Customer Orders
   - ✅ Get Order
   - ✅ Cancel Order
   - ✅ Get Order Status
   - ❌ ~~Delete Profile~~ - REMOVED

3. **3. Kitchen Staff** (4 endpoints)
   - ✅ Get Pending Orders
   - ✅ Accept Order
   - ✅ Mark Order Ready
   - ✅ Reject Order

4. **4. Delivery Driver** (2 endpoints)
   - ✅ Mark Order Picked Up
   - ✅ Mark Order Delivered

5. **5. Admin** (6 endpoints)
   - ✅ Get All Customers
   - ✅ Create Admin Invitation
   - ✅ Add Menu Item ← Should be here now
   - ✅ Update Menu Item ← Should be here now
   - ✅ Delete Menu Item ← Should be here now

6. **6. Public - Restaurants** (3 endpoints)
   - ✅ Get All Restaurants
   - ✅ Get Restaurant by ID
   - ✅ Get Restaurant Menu
   - ❌ Menu management endpoints - MOVED to Admin

---

## ✅ Step 3: Test Public Endpoints (No Auth)

```bash
# Should work - Get restaurants
curl http://localhost:8080/api/restaurants

# Should work - Get restaurant menu
curl http://localhost:8080/api/restaurants/1/menu
```

**Expected:** ✅ 200 OK

---

## ✅ Step 4: Test Removed Endpoints

```bash
# Should fail - Generic register removed
curl -X POST http://localhost:8080/api/auth/register

# Should fail - Delete profile removed  
curl -X DELETE http://localhost:8080/api/customer/1

# Should fail - Old menu management paths
curl -X POST http://localhost:8080/api/restaurants/1/menu
```

**Expected:** ❌ 404 Not Found or 401 Unauthorized

---

## ✅ Step 5: Test New Admin Menu Endpoints

```bash
# Login as admin first
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"pass"}' \
  | jq -r '.token')

# Should work - Add menu item (new path)
curl -X POST http://localhost:8080/api/admin/restaurants/1/menu \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Pizza",
    "description": "Delicious",
    "price": 12.99,
    "category": "MAIN_COURSE"
  }'
```

**Expected:** ✅ 200 OK

---

## ✅ Step 6: Verify Swagger Organization

**Check these are NOT visible:**
- ❌ "Authentication" (without number)
- ❌ "Customer Orders"
- ❌ "Customer Profile"  
- ❌ "Kitchen Operations"
- ❌ "Delivery Operations"
- ❌ "Menu Management"
- ❌ "Restaurants" (without "Public -")

**Should only see:**
- ✅ "1. Authentication"
- ✅ "2. Customer"
- ✅ "3. Kitchen Staff"
- ✅ "4. Delivery Driver"
- ✅ "5. Admin"
- ✅ "6. Public - Restaurants"

---

## Quick Test Script

```bash
#!/bin/bash

echo "=== Testing API Cleanup ==="

echo -e "\n1. Testing public endpoint (should work)..."
curl -s http://localhost:8080/api/restaurants | head -n 5

echo -e "\n2. Testing removed generic register (should fail)..."
curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/api/auth/register

echo -e "\n3. Testing removed delete endpoint (should fail)..."
curl -s -o /dev/null -w "%{http_code}" -X DELETE http://localhost:8080/api/customer/1

echo -e "\n4. Testing old menu path (should fail)..."
curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/api/restaurants/1/menu

echo -e "\n5. Testing role-specific register (should work)..."
curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/api/auth/register/customer \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test123",
    "email": "test@test.com",
    "password": "Pass123!",
    "name": "Test User",
    "phone": "+1234567890",
    "address": "123 Test St"
  }'

echo -e "\n\n=== Done ==="
```

---

## Success Criteria

✅ Swagger shows exactly 6 numbered sections  
✅ No duplicate or unnumbered sections  
✅ Public endpoints work without auth  
✅ Admin menu endpoints require auth  
✅ Removed endpoints return 404  
✅ New admin paths work correctly  
✅ Role-specific registration works  
✅ Deactivate endpoint works (delete removed)  

---

## If Something is Wrong

### Clear Browser Cache
```
Ctrl + Shift + Delete (Windows/Linux)
Cmd + Shift + Delete (Mac)
```

### Hard Refresh
```
Ctrl + F5 (Windows/Linux)
Cmd + Shift + R (Mac)
```

### Check Application Logs
```bash
# Look for startup errors
tail -f logs/spring.log

# Or in console output
```

### Verify Files Were Modified
```bash
# Check if changes were applied
git diff src/main/java/com/mavic/backend/auth/controller/AuthController.java
git diff src/main/java/com/mavic/backend/customer/controller/CustomerController.java
```

---

## Documentation

- ✅ `API_CLEANUP_SUMMARY.md` - Complete list of changes
- ✅ `SWAGGER_ORGANIZATION.md` - Swagger structure
- ✅ `HOW_TO_ENABLE_EMAIL.md` - Email activation guide
- ✅ `ADMIN_INVITATION_SYSTEM.md` - Admin invitation feature

All documentation is up to date! 🎉
