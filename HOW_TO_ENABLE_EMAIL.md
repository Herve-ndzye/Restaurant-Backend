# How to Enable Email Notifications

## Quick Guide to Activate Email

### Step 1: Get Email Credentials

#### Option A: Using Gmail (Easiest for Testing)

1. **Enable 2-Factor Authentication**
   - Go to: https://myaccount.google.com/security
   - Enable "2-Step Verification"

2. **Generate App Password**
   - Go to: https://myaccount.google.com/apppasswords
   - Select: "Mail" and "Other (Custom name)"
   - Name it: "Restaurant Management System"
   - Click "Generate"
   - **Copy the 16-character password** (example: `abcd efgh ijkl mnop`)

#### Option B: Using SendGrid (Best for Production)

1. Sign up at https://sendgrid.com (free tier: 100 emails/day)
2. Create API key in Settings → API Keys
3. Use these settings:
   - Host: `smtp.sendgrid.net`
   - Port: `587`
   - Username: `apikey`
   - Password: `<your-api-key>`

### Step 2: Update .env File

Open your `.env` file and update these lines:

```env
# Change this from false to true
MAIL_ENABLED=true

# Add your email settings
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-16-char-app-password

# Optional: Customize
APP_NAME=Restaurant Management System
FRONTEND_URL=http://localhost:3000
```

### Step 3: Restart Application

```bash
./mvnw spring-boot:run
```

### Step 4: Test Email

Create an admin invitation:

```bash
curl -X POST http://localhost:8080/api/admin/create-admin \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_admin",
    "email": "test@example.com",
    "name": "Test Admin"
  }'
```

Check the email inbox for `test@example.com` - you should receive a professional HTML email!

### Step 5: Verify in Logs

Look for these success messages:

```
INFO  - Admin invitation created by admin_user for new admin: test_admin
INFO  - Invitation email sent to: test@example.com
```

---

## Complete .env Configuration

### For Gmail:

```env
# Email Configuration
MAIL_ENABLED=true
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=abcd efgh ijkl mnop

# Application
APP_NAME=Restaurant Management System
FRONTEND_URL=http://localhost:3000
```

### For SendGrid:

```env
# Email Configuration
MAIL_ENABLED=true
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=SG.your-sendgrid-api-key-here

# Application
APP_NAME=Restaurant Management System
FRONTEND_URL=http://localhost:3000
```

### For Outlook/Office 365:

```env
# Email Configuration
MAIL_ENABLED=true
MAIL_HOST=smtp.office365.com
MAIL_PORT=587
MAIL_USERNAME=your-email@outlook.com
MAIL_PASSWORD=your-password

# Application
APP_NAME=Restaurant Management System
FRONTEND_URL=http://localhost:3000
```

---

## What Happens When Email is Enabled?

### Admin Invitation Flow:

1. **Existing admin creates invitation** via API
2. **System generates secure temporary password**
3. **Email automatically sent** to new admin with:
   - Welcome message
   - Username and temporary password
   - Login button/link
   - Security instructions
   - Step-by-step guide
4. **API response still includes credentials** (backup)
5. **New admin receives email** and can login immediately

### Password Change Flow:

1. **User changes password** via API
2. **Confirmation email sent** automatically
3. Includes timestamp and security alert

---

## Troubleshooting

### Email Not Sending?

**Check 1: Environment Variables**
```bash
# Windows
echo %MAIL_USERNAME%
echo %MAIL_ENABLED%

# Linux/Mac
echo $MAIL_USERNAME
echo $MAIL_ENABLED
```

**Check 2: Application Logs**
Look for:
```
WARN  - Email service not configured. Skipping invitation email
```
or
```
ERROR - Failed to send invitation email to: ...
```

**Check 3: Gmail App Password**
- Must use App Password, NOT regular password
- Must have 2FA enabled first
- Remove spaces from app password: `abcd efgh ijkl mnop` → `abcdefghijklmnop`

**Check 4: Port 587**
- Ensure port 587 is not blocked by firewall
- Try telnet test: `telnet smtp.gmail.com 587`

### Email Goes to Spam?

**Solutions:**
- Ask recipients to whitelist your sender email
- Use professional email service (SendGrid, AWS SES)
- Set up SPF/DKIM records (for production)

### Still Not Working?

**Use MailHog for Testing:**

```bash
# Install MailHog
docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog

# Update .env
MAIL_ENABLED=true
MAIL_HOST=localhost
MAIL_PORT=1025
MAIL_USERNAME=test
MAIL_PASSWORD=test

# View emails at: http://localhost:8025
```

MailHog captures all emails without sending them - perfect for local development!

---

## Email Templates

### Admin Invitation Email Preview:

**Subject:** Welcome to Restaurant Management System - Admin Account Created

**Content:**
- 🎉 Welcome header
- Personal greeting
- Credentials box with username/password
- ⚠️ Security warnings
- 🔗 Login button
- 📝 Step-by-step instructions
- Help/support info

### Password Change Confirmation:

**Subject:** Password Changed Successfully - Restaurant Management System

**Content:**
- ✅ Success message
- Timestamp
- Security alert
- Contact info

---

## Benefits of Email

✅ **Better UX:** No manual credential sharing  
✅ **More Secure:** Credentials delivered via encrypted email  
✅ **Professional:** Great first impression  
✅ **Automated:** No manual work required  
✅ **Reliable:** Async processing, doesn't block API  
✅ **Audit Trail:** Email history for security  

---

## Summary

**To Enable Email:**

1. Get Gmail App Password (or SendGrid API key)
2. Update `.env`: Set `MAIL_ENABLED=true` + credentials
3. Restart application
4. Test by creating admin invitation
5. Check recipient inbox

**That's it! 🚀**

For detailed configuration options, see `EMAIL_SETUP.md`.
