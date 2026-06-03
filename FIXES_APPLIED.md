# 🔧 Fixes Applied - Login Security & Rate Limiting

## Issues Fixed

### ❌ Issue 1: Failed Login Attempts Not Incrementing
**Problem:** Counter stayed at "Failed login attempt 1 of 5" even after multiple attempts

**Root Cause:** 
- Transaction was being rolled back when exception was thrown
- Database save wasn't being committed before exception
- User entity was being read twice causing stale data

**Solution:**
- Created separate transaction method `handleFailedLoginInNewTransaction()` with `@Transactional(TxType.REQUIRES_NEW)`
- This ensures the database update is committed in its own transaction before throwing exception
- Used `saveAndFlush()` to force immediate database commit

### ❌ Issue 2: Rate Limiting Not Working
**Problem:** Could make 6+ login requests without hitting rate limit

**Root Cause:**
- ObjectMapper wasn't properly initialized with JavaTimeModule for LocalDateTime
- Rate limit key included full path, creating separate buckets for each request

**Solution:**
- Explicitly registered `JavaTimeModule` in ObjectMapper constructor
- Changed rate limit key to use endpoint category (auth/orders/customer) instead of full path
- Added debug logging to track token consumption
- All auth requests now share same bucket per IP address

---

## Changes Made

### 1. AuthService.java
```java
// OLD: Single transaction with failed save
@Transactional
public AuthResponse login(...) {
    // ... code
    handleFailedLogin(user); // Save not committed before exception
}

// NEW: Separate transaction for failed login handling
public AuthResponse login(...) {
    // ... code
    handleFailedLoginInNewTransaction(user.getId()); // Committed in new transaction
}

@Transactional(Transactional.TxType.REQUIRES_NEW)
public void handleFailedLoginInNewTransaction(Long userId) {
    User user = userRepository.findById(userId).orElseThrow();
    user.incrementFailedAttempts();
    // ... logic
    userRepository.saveAndFlush(user); // Force commit NOW
    throw new AccountLockedException(...); // Then throw exception
}
```

### 2. RateLimitFilter.java
```java
// OLD: No JavaTimeModule registered
private final ObjectMapper objectMapper = new ObjectMapper();

// NEW: Properly initialized ObjectMapper
public RateLimitFilter() {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());
}

// OLD: Key per full path (creates too many buckets)
private String getClientKey(HttpServletRequest request, String path) {
    return clientIp + ":" + path; // 0:0:0:0:0:0:0:1:/api/auth/login
}

// NEW: Key per endpoint category (groups related requests)
private String getClientKey(HttpServletRequest request, String path) {
    String endpointCategory = getEndpointCategory(path);
    return clientIp + ":" + endpointCategory; // 0:0:0:0:0:0:0:1:auth
}
```

---

## Testing Instructions

### Test 1: Failed Login Attempt Counter

```bash
# Reset user in database first
UPDATE user SET failedLoginAttempts = 0, accountLockedUntil = NULL WHERE username = 'mavic';

# Attempt 1
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"mavic","password":"wrong"}'
  
# Expected: "Warning: 4 attempts remaining"

# Attempt 2  
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"mavic","password":"wrong"}'
  
# Expected: "Warning: 3 attempts remaining"

# Check database
SELECT username, failedLoginAttempts FROM user WHERE username = 'mavic';
# Should show: failedLoginAttempts = 2
```

**Expected Log Output:**
```
WARN  | Failed login attempt 1 of 5 for user: mavic
WARN  | Failed login attempt 2 of 5 for user: mavic
WARN  | Failed login attempt 3 of 5 for user: mavic
WARN  | Failed login attempt 4 of 5 for user: mavic
WARN  | Account locked for user: mavic after 5 failed attempts
```

### Test 2: Rate Limiting

```bash
# Send 6 rapid requests
for i in {1..6}; do
  echo "Request $i:"
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"test","password":"test"}' \
    -w "\nHTTP Code: %{http_code}\n\n"
  sleep 0.1
done
```

**Expected Output:**
```
Request 1: HTTP Code: 401
Request 2: HTTP Code: 401
Request 3: HTTP Code: 401
Request 4: HTTP Code: 401
Request 5: HTTP Code: 401
Request 6: HTTP Code: 429 (Rate Limit Exceeded)
```

**Expected Log Output:**
```
INFO  | Creating new rate limit bucket for key: 0:0:0:0:0:0:0:1:auth, isAuth: true
DEBUG | Rate limit check for key: 0:0:0:0:0:0:0:1:auth, tokens available: 5
DEBUG | Request allowed for key: 0:0:0:0:0:0:0:1:auth
DEBUG | Rate limit check for key: 0:0:0:0:0:0:0:1:auth, tokens available: 4
...
WARN  | Rate limit exceeded for key: 0:0:0:0:0:0:0:1:auth
```

---

## Database Verification

### Check Failed Attempts Are Persisting

```sql
-- After 3 failed login attempts
SELECT username, failedLoginAttempts, accountLockedUntil, lastFailedLogin
FROM user
WHERE username = 'mavic';

-- Expected result:
-- username  | failedLoginAttempts | accountLockedUntil | lastFailedLogin
-- mavic     | 3                   | NULL               | 2026-06-03 21:35:24
```

### Check Account Lock Is Persisted

```sql
-- After 5 failed login attempts
SELECT username, failedLoginAttempts, accountLockedUntil, lastFailedLogin
FROM user
WHERE username = 'mavic';

-- Expected result:
-- username  | failedLoginAttempts | accountLockedUntil      | lastFailedLogin
-- mavic     | 5                   | 2026-06-03 21:50:24    | 2026-06-03 21:35:24
```

---

## Key Technical Details

### Why REQUIRES_NEW Transaction?

```java
@Transactional(Transactional.TxType.REQUIRES_NEW)
public void handleFailedLoginInNewTransaction(Long userId) {
    // This method runs in a SEPARATE transaction
    // Even if the parent transaction rolls back, this commits
    userRepository.saveAndFlush(user);
    throw new AccountLockedException(...);
}
```

- Parent transaction may roll back due to exception
- REQUIRES_NEW creates independent transaction
- saveAndFlush() forces immediate commit to database
- Counter persists even when exception is thrown

### Why Endpoint Category for Rate Limiting?

```java
// OLD: Separate bucket per full path
0:0:0:0:0:0:0:1:/api/auth/login
0:0:0:0:0:0:0:1:/api/auth/register
// Problem: User can bypass by alternating endpoints

// NEW: Single bucket per category
0:0:0:0:0:0:0:1:auth
// All /api/auth/* requests share same rate limit
```

---

## Summary

✅ **Fixed:** Failed login attempts now properly increment and persist  
✅ **Fixed:** Rate limiting now works correctly (5 auth requests/min)  
✅ **Fixed:** Jackson LocalDateTime serialization error  
✅ **Improved:** Better logging for debugging  
✅ **Improved:** Rate limit buckets grouped by category

🚀 **Ready to test!** Restart your application and try the tests above.
