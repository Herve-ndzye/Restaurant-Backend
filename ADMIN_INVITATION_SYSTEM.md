# Admin Invitation System with Email Notifications

## Overview
The admin invitation system allows existing `RESTAURANT_ADMIN` users to create new admin accounts with secure temporary credentials. **New admins automatically receive a professional HTML email** with their login credentials and onboarding instructions. They are required to change their password on first login.

## Features

### 1. **Admin Account Creation** (Existing Admin Only)
- Only users with `RESTAURANT_ADMIN` role can create new admin accounts
- Existing admin provides new admin details (username, email, name, optional restaurantId)
- System generates a secure temporary password automatically
- **Email automatically sent to new admin with credentials** 📧
- API response also includes credentials for manual sharing (backup)
- Audit log records who created the invitation

### 2. **Automatic Email Notification** 🆕
- **Professional HTML email** sent immediately to new admin
- Email includes:
  - Welcome message with branding
  - Username and temporary password
  - Direct login link to frontend
  - Security warnings and best practices
  - Step-by-step onboarding instructions
  - Contact information for help
- **Asynchronous sending** - doesn't block API response
- **Graceful failure** - if email fails, credentials still returned in API response

### 3. **Secure Temporary Passwords**
- Auto-generated with strong requirements:
  - 3 uppercase letters
  - 3 lowercase letters
  - 2 digits
  - 2 special characters (@$!%*?&)
  - Randomly shuffled for security
- Example: `Xy3$aB9z@K`

### 4. **First Login Password Change**
- New admin accounts are flagged with `firstLogin = true`
- Login response includes `firstLogin` flag to signal frontend
- New admin must use change password endpoint before full access
- Password change clears the `firstLogin` flag
- **Confirmation email sent after password change**

## Workflow

### Step 1: Existing Admin Creates Invitation
1. Existing admin logs into the system
2. Navigates to admin management section
3. Fills out new admin details form (username, email, name)
4. Submits POST request to `/api/admin/create-admin`
5. System creates account and **sends email automatically**
6. Receives API response with credentials (backup)

### Step 2: New Admin Receives Email 📧
1. New admin checks their email inbox
2. Receives professional HTML email with:
   - Welcome message
   - Username: `jane_admin`
   - Temporary Password: `Xy3$aB9z@K`
   - "Login Now" button linking to frontend
   - Security instructions
   - Step-by-step guide

### Step 3: New Admin First Login
1. New admin clicks "Login Now" button in email (or navigates manually)
2. Enters username and temporary password from email
3. System authenticates and returns JWT token
4. Login response includes `firstLogin: true`
5. Frontend detects flag and redirects to password change page

### Step 4: Password Change
1. New admin enters:
   - Current password (temporary password from email)
   - New secure password
   - Confirmation of new password
2. System validates and updates password
3. `firstLogin` flag cleared
4. **Confirmation email sent to admin**

### Step 5: Normal Usage
1. New admin can now login with their chosen password
2. `firstLogin` flag is `false`
3. Full access to admin features

## Email Templates

### Admin Invitation Email

**From:** noreply@restaurant.com  
**Subject:** Welcome to Restaurant Management System - Admin Account Created

**Preview:**
```
🎉 Welcome to Restaurant Management System!

Hi Jane Doe,

You've been invited by John Admin to join as a Restaurant Administrator.

🔑 Your Login Credentials
━━━━━━━━━━━━━━━━━━━━━━━━━━━
Username
jane_admin

Temporary Password
Xy3$aB9z@K

⚠️ Important Security Notice:
• This is a temporary password
• You must change it on your first login
• Never share your password with anyone
• This email contains sensitive information

[Login Now]  ← Button links to http://localhost:3000/login

📝 Next Steps:
→ Visit the login page
→ Enter your username and temporary password
→ You'll be prompted to change your password
→ Choose a strong, secure password
→ Start managing your restaurant!

Need help?
If you have any questions or didn't request this account,
please contact your system administrator immediately.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
This is an automated message from Restaurant Management System.
Please do not reply to this email.
© 2026 Restaurant Management System. All rights reserved.
```

### Password Change Confirmation Email

**Subject:** Password Changed Successfully - Restaurant Management System

```
✅ Password Changed Successfully

Hi jane_admin,

✓ Your password has been changed successfully.
Time: 2026-06-05T10:30:00

If you did not make this change, please contact your 
system administrator immediately.

© 2026 Restaurant Management System. All rights reserved.
```

## API Endpoints

### Create Admin Invitation
```
POST /api/admin/create-admin
Authorization: Bearer <RESTAURANT_ADMIN_TOKEN>
Content-Type: application/json

{
  "username": "jane_admin",
  "email": "jane@restaurant.com",
  "name": "Jane Doe",
  "restaurantId": 1  // optional
}
```

**Response (201 Created):**
```json
{
  "userId": 10,
  "username": "jane_admin",
  "email": "jane@restaurant.com",
  "temporaryPassword": "Xy3$aB9z@K",
  "loginUrl": "/api/auth/login",
  "message": "Admin account created successfully. Invitation email has been sent to jane@restaurant.com. You can also share these credentials manually if needed.",
  "requirePasswordChange": true
}
```

**Note:** Email sent asynchronously. If email fails, check logs but credentials are still in response.

### Login (First Time)
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "jane_admin",
  "password": "Xy3$aB9z@K"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 10,
  "username": "jane_admin",
  "email": "jane@restaurant.com",
  "role": "RESTAURANT_ADMIN",
  "firstLogin": true  // Frontend should redirect to password change
}
```

### Change Password
```
PUT /api/auth/change-password
Authorization: Bearer <USER_TOKEN>
Content-Type: application/json

{
  "currentPassword": "Xy3$aB9z@K",
  "newPassword": "MyNewSecurePass123!",
  "confirmPassword": "MyNewSecurePass123!"
}
```

**Response (200 OK):**
```json
{
  "message": "Password changed successfully"
}
```

**Note:** Confirmation email sent automatically.

## Setup Requirements

### 1. Add Email Dependency (Already Done)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

### 2. Configure Email in .env
```env
# Email Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Application Configuration
APP_NAME=Restaurant Management System
FRONTEND_URL=http://localhost:3000
```

**For Gmail:**
1. Enable 2-Factor Authentication
2. Generate App Password at https://myaccount.google.com/apppasswords
3. Use app password in MAIL_PASSWORD

### 3. Email Service Configuration (application.yaml)
Already configured:
```yaml
spring:
  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

**See EMAIL_SETUP.md for complete configuration guide**

## Security Features

### 1. **Role-Based Access Control**
- Only `RESTAURANT_ADMIN` can create admin invitations
- Enforced at both controller and service layers
- Security configuration restricts endpoint access

### 2. **Strong Password Requirements**
- Temporary passwords meet strict security standards
- New passwords must contain:
  - At least 8 characters
  - One uppercase letter
  - One lowercase letter
  - One digit
  - One special character (@$!%*?&)

### 3. **Email Security**
- Emails sent via TLS/SSL
- Credentials never logged
- Async sending prevents blocking
- Graceful failure handling

### 4. **Audit Logging**
- All admin invitation creations are logged
- Tracks who created the invitation
- Tracks when password changes occur
- Email sending status logged

### 5. **Forced Password Change**
- `firstLogin` flag prevents new admins from using temporary passwords indefinitely
- Frontend should enforce password change flow
- Backend tracks status to ensure compliance

## Testing

### 1. Test Email Configuration
```bash
# Set environment variables
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password

# Start application
./mvnw spring-boot:run
```

### 2. Create Test Invitation
```bash
# Login as existing admin
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "existing_admin",
    "password": "AdminPass123!"
  }'

# Create invitation (replace TOKEN)
curl -X POST http://localhost:8080/api/admin/create-admin \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_admin",
    "email": "test@example.com",
    "name": "Test Admin"
  }'
```

### 3. Check Email
- Check inbox for test@example.com
- Verify email received
- Check formatting and links
- Verify credentials work

### 4. Check Logs
```
INFO  - Admin invitation created by existing_admin for new admin: test_admin
INFO  - Invitation email sent to: test@example.com
```

Or if failed:
```
ERROR - Failed to send invitation email to test@example.com: ...
```

## Troubleshooting

### Email Not Sending

**1. Check Environment Variables:**
```bash
echo $MAIL_USERNAME
echo $MAIL_HOST
```

**2. Check Application Logs:**
Look for error messages about email failures

**3. Verify SMTP Credentials:**
- Gmail: Must use App Password, not regular password
- Enable 2FA first
- Test with email client

**4. Check Network:**
- Port 587 must be open
- Check firewall rules

**5. Use MailHog for Testing:**
```bash
# Install MailHog
docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog

# Configure .env
MAIL_HOST=localhost
MAIL_PORT=1025
MAIL_USERNAME=test
MAIL_PASSWORD=test

# View emails at http://localhost:8025
```

### Email in Spam

- Use professional email service (SendGrid, AWS SES)
- Set up SPF/DKIM records
- Use dedicated domain
- Ask recipients to whitelist

## Production Recommendations

1. **Use Professional Email Service**
   - SendGrid (100 emails/day free)
   - AWS SES ($0.10 per 1,000 emails)
   - Mailgun (5,000 emails/month free)

2. **Configure DNS Records**
   - SPF
   - DKIM
   - DMARC

3. **Use Dedicated Email**
   ```env
   MAIL_USERNAME=noreply@yourdomain.com
   ```

4. **Monitor Email Delivery**
   - Track bounce rates
   - Monitor spam complaints
   - Set up alerts

5. **Customize Templates**
   - Add company logo
   - Update branding colors
   - Customize footer
   - Add social links

## Benefits of Email Automation

✅ **Better User Experience**
- No manual credential sharing
- Immediate onboarding
- Professional first impression
- Clear instructions

✅ **Security**
- Credentials delivered securely
- Email trail for audit
- No credential exposure in UI
- Confirmation of changes

✅ **Scalability**
- Automated process
- No manual intervention
- Handles high volume
- Async processing

✅ **Reliability**
- Fallback to API response
- Graceful error handling
- Retry capability
- Comprehensive logging

## Future Enhancements

1. **Email Verification**: Require email verification before account activation
2. **Password Expiration**: Add expiration time to temporary passwords
3. **Invitation Links**: One-time use activation links instead of passwords
4. **Email Templates**: HTML templates with Thymeleaf
5. **Analytics**: Track email open/click rates
6. **Internationalization**: Multi-language email support
7. **Rich Content**: Attach PDF guides, embed images
8. **Calendar Invites**: Include training session invites

## Related Documentation

- **EMAIL_SETUP.md**: Complete email configuration guide
- **REGISTRATION_REFACTOR.md**: Role-specific registration system
- **ACCOUNT_DEACTIVATION.md**: Account deactivation feature

## Support

For issues:
1. Check EMAIL_SETUP.md for configuration help
2. Review application logs for errors
3. Test with MailHog for local development
4. Verify SMTP credentials
5. Check network/firewall settings
