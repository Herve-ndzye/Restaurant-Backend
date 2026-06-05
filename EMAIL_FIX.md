# Email Configuration Fix

## Problem
Application failed to start because `MAIL_USERNAME` was required but not configured.

## Solution
Email functionality is now **optional** and disabled by default.

## Changes Made

### 1. EmailService - Made Email Optional
- Added `@Autowired(required = false)` for JavaMailSender
- Added `emailEnabled` flag check
- If email not configured, logs warning and continues
- Graceful degradation: API still returns credentials for manual sharing

### 2. MailConfig - Conditional Configuration
- Created `MailConfig.java` with `@ConditionalOnProperty`
- JavaMailSender only created if `spring.mail.enabled=true`
- Prevents startup failures when email not configured

### 3. application.yaml - Optional Email
- Added `spring.mail.enabled` property (default: false)
- Made `MAIL_USERNAME` and `MAIL_PASSWORD` optional with empty defaults

### 4. .env Files - Added MAIL_ENABLED Flag
- Added `MAIL_ENABLED=false` to disable email by default
- Clear documentation about email being optional

## How to Use

### Option 1: Without Email (Default - Development)

**Your .env file:**
```env
MAIL_ENABLED=false
```

**Behavior:**
- Application starts successfully ✅
- Admin invitation API works ✅
- Credentials returned in API response ✅
- No email sent (warning logged) ⚠️
- Existing admin manually shares credentials

**API Response includes credentials:**
```json
{
  "userId": 10,
  "username": "new_admin",
  "email": "new@admin.com",
  "temporaryPassword": "Xy3$aB9z@K",  ← Share this manually
  "message": "Admin account created successfully. Invitation email has been sent to new@admin.com. You can also share these credentials manually if needed."
}
```

### Option 2: With Email (Production)

**Your .env file:**
```env
MAIL_ENABLED=true
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

**Behavior:**
- Application starts successfully ✅
- Admin invitation API works ✅
- Credentials returned in API response ✅
- Professional HTML email sent automatically 📧
- Existing admin can still share credentials manually as backup

## Quick Start

### 1. Start Application (No Email Configuration Needed)
```bash
./mvnw spring-boot:run
```

Application will start successfully with email disabled.

### 2. Test Admin Invitation
```bash
# Login as existing admin
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "your_admin",
    "password": "your_password"
  }'

# Create invitation (use token from above)
curl -X POST http://localhost:8080/api/admin/create-admin \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "new_admin",
    "email": "new@example.com",
    "name": "New Admin"
  }'
```

### 3. Response Includes Credentials
```json
{
  "userId": 5,
  "username": "new_admin",
  "email": "new@example.com",
  "temporaryPassword": "Xy3$aB9z@K",
  "loginUrl": "/api/auth/login",
  "message": "Admin account created successfully...",
  "requirePasswordChange": true
}
```

**Copy the temporaryPassword and share it with the new admin manually.**

## Logs

### With Email Disabled
```
WARN  - Email service not configured. Skipping invitation email to: new@example.com
INFO  - Admin invitation created by admin_user for new admin: new_admin
```

### With Email Enabled
```
INFO  - Admin invitation created by admin_user for new admin: new_admin
INFO  - Invitation email sent to: new@example.com
```

## Enable Email Later

When ready to enable automatic emails:

### 1. Update .env
```env
MAIL_ENABLED=true
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

### 2. For Gmail - Get App Password
1. Enable 2-Factor Authentication: https://myaccount.google.com/security
2. Generate App Password: https://myaccount.google.com/apppasswords
3. Use app password in `MAIL_PASSWORD`

### 3. Restart Application
```bash
./mvnw spring-boot:run
```

### 4. Test Email
Create admin invitation - email will be sent automatically!

## Production Setup

See **EMAIL_SETUP.md** for:
- Gmail configuration
- SendGrid setup (recommended for production)
- AWS SES setup
- Email template customization
- Troubleshooting guide
- Best practices

## Benefits of This Approach

✅ **No Breaking Changes**
- Application starts without email configuration
- Email is opt-in, not required

✅ **Development Friendly**
- No need to configure SMTP for local development
- Credentials in API response for testing

✅ **Production Ready**
- Enable email when ready
- Professional automated emails
- Fallback to manual sharing

✅ **Flexible**
- Use email in production
- Skip email in development
- Switch anytime without code changes

## Summary

**Before:** Application crashed if email not configured  
**After:** Application works perfectly without email, credentials returned in API response

**Email is now optional:**
- Default: Disabled (perfect for development)
- Enable anytime: Set `MAIL_ENABLED=true` + SMTP config
- Always works: Credentials always in API response as backup
