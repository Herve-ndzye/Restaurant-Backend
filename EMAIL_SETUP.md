# Email Configuration Guide

## Overview
The admin invitation system now automatically sends professional HTML emails to new administrators with their login credentials. This guide explains how to configure email functionality.

## Features

### ✅ Automatic Email Notifications
- **Admin Invitation Email**: Sent when existing admin creates new admin account
  - Professional HTML template with branding
  - Includes username and temporary password
  - Direct login link to frontend
  - Security warnings and instructions
  - Step-by-step onboarding guide

- **Password Change Confirmation**: Sent after successful password change
  - Confirms password was changed
  - Includes timestamp
  - Security alert if unauthorized

### ✅ Async Processing
- Emails sent asynchronously (non-blocking)
- Won't slow down API responses
- Graceful failure handling

### ✅ Graceful Degradation
- If email fails, operation still succeeds
- Admin can manually share credentials from API response
- Errors logged for monitoring

## Email Configuration

### Option 1: Gmail (Recommended for Development/Testing)

#### Step 1: Enable 2-Factor Authentication
1. Go to your Google Account settings
2. Navigate to Security
3. Enable 2-Step Verification

#### Step 2: Generate App Password
1. Go to https://myaccount.google.com/apppasswords
2. Select "Mail" and "Other (Custom name)"
3. Name it "Restaurant Management System"
4. Click "Generate"
5. Copy the 16-character password

#### Step 3: Update .env File
```env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-16-char-app-password
```

### Option 2: Outlook/Office 365

```env
MAIL_HOST=smtp.office365.com
MAIL_PORT=587
MAIL_USERNAME=your-email@outlook.com
MAIL_PASSWORD=your-password
```

### Option 3: Custom SMTP Server

```env
MAIL_HOST=mail.yourdomain.com
MAIL_PORT=587
MAIL_USERNAME=noreply@yourdomain.com
MAIL_PASSWORD=your-smtp-password
```

### Option 4: SendGrid (Production Recommended)

```env
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=your-sendgrid-api-key
```

### Option 5: AWS SES (Production Recommended)

```env
MAIL_HOST=email-smtp.us-east-1.amazonaws.com
MAIL_PORT=587
MAIL_USERNAME=your-ses-smtp-username
MAIL_PASSWORD=your-ses-smtp-password
```

## Configuration Parameters

### Required Environment Variables

```env
# SMTP Server Configuration
MAIL_HOST=smtp.gmail.com              # SMTP server hostname
MAIL_PORT=587                         # SMTP port (usually 587 for TLS)
MAIL_USERNAME=your-email@gmail.com    # SMTP username/email
MAIL_PASSWORD=your-password           # SMTP password/app password

# Application Configuration
APP_NAME=Restaurant Management System  # App name shown in emails
FRONTEND_URL=http://localhost:3000    # Frontend URL for login links
```

### Optional Configuration (in application.yaml)

Already configured with sensible defaults:
```yaml
spring:
  mail:
    properties:
      mail:
        smtp:
          auth: true                    # Enable authentication
          starttls:
            enable: true                # Enable TLS
            required: true              # Require TLS
          connectiontimeout: 5000       # Connection timeout (ms)
          timeout: 5000                 # Read timeout (ms)
          writetimeout: 5000            # Write timeout (ms)
    test-connection: false              # Don't test on startup
```

## Email Templates

### Admin Invitation Email

**Subject:** Welcome to Restaurant Management System - Admin Account Created

**Content:**
- Welcome header with app branding
- Personal greeting
- Who invited them
- Credentials box with username and temporary password
- Security warnings
- Login button (links to frontend)
- Step-by-step onboarding instructions
- Help/support information
- Professional footer

**Example:**
```
🎉 Welcome to Restaurant Management System!

Hi Jane Doe,

You've been invited by John Admin to join as a Restaurant Administrator.

🔑 Your Login Credentials
━━━━━━━━━━━━━━━━━━━━━
Username: jane_admin
Temporary Password: Xy3$aB9z@K

⚠️ Important Security Notice:
• This is a temporary password
• You must change it on your first login
• Never share your password with anyone

[Login Now Button]

📝 Next Steps:
→ Visit the login page
→ Enter your username and temporary password
→ You'll be prompted to change your password
→ Choose a strong, secure password
→ Start managing your restaurant!
```

### Password Change Confirmation Email

**Subject:** Password Changed Successfully - Restaurant Management System

**Content:**
- Success notification
- Timestamp of change
- Security alert message
- Contact information

## Testing Email Configuration

### 1. Quick Test with cURL

```bash
# Create admin invitation (triggers email)
curl -X POST http://localhost:8080/api/admin/create-admin \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_admin",
    "email": "test@example.com",
    "name": "Test Admin"
  }'
```

### 2. Check Application Logs

Look for these log messages:
```
INFO  - Admin invitation created by existing_admin for new admin: test_admin
INFO  - Invitation email sent to: test@example.com
```

Or if it fails:
```
ERROR - Failed to send invitation email to test@example.com: ...
```

### 3. Verify Email Received

Check the recipient's inbox:
- Should receive professional HTML email
- Should contain credentials
- Login button should work
- All links should be correct

## Troubleshooting

### Problem: Email not sending

**Check 1: Environment Variables**
```bash
# Verify variables are set
echo $MAIL_USERNAME
echo $MAIL_HOST
```

**Check 2: Application Logs**
Look for error messages:
```
ERROR - Failed to send invitation email to ...
```

**Check 3: SMTP Credentials**
- Gmail: Ensure using App Password, not regular password
- Verify 2FA is enabled for Gmail
- Test credentials with email client

**Check 4: Firewall/Network**
- Port 587 must be open
- Check corporate firewall rules
- Try telnet: `telnet smtp.gmail.com 587`

**Check 5: SMTP Server Limits**
- Gmail: 500 emails/day for free accounts
- Check daily sending limits

### Problem: Email in spam folder

**Solutions:**
- Set up SPF records for your domain
- Set up DKIM signing
- Use professional email service (SendGrid, AWS SES)
- Ask recipients to whitelist sender

### Problem: Async emails not working

**Check:**
- `@EnableAsync` annotation in BackendApplication.java
- Spring creates thread pool automatically
- Check logs for async errors

### Problem: Template not rendering

**Check:**
- HTML is valid
- Variables are properly substituted
- Content-Type is set to text/html
- MimeMessageHelper configured correctly

## Production Recommendations

### 1. Use Professional Email Service

**Why:**
- Better deliverability
- Higher sending limits
- Built-in analytics
- Spam protection
- Reputation management

**Options:**
- **SendGrid**: Easy setup, good free tier
- **AWS SES**: Cost-effective, high volume
- **Mailgun**: Developer-friendly
- **Postmark**: Excellent deliverability

### 2. Configure SPF/DKIM/DMARC

Add DNS records for your domain:
```
TXT @ "v=spf1 include:_spf.google.com ~all"
```

### 3. Use Dedicated Email Address

```env
MAIL_USERNAME=noreply@restaurant.com
```

Not personal email.

### 4. Monitor Email Delivery

- Set up logging/monitoring
- Track bounce rates
- Monitor spam complaints
- Set up alerts for failures

### 5. Template Customization

Edit `EmailService.java` to customize:
- Branding/colors
- Logo images
- Footer content
- Support links
- Social media links

### 6. Rate Limiting

Consider adding rate limits to prevent abuse:
```java
@RateLimiter(name = "email", fallbackMethod = "emailFallback")
public void sendAdminInvitationEmail(...) {
    // ...
}
```

### 7. Email Queuing

For high volume, use message queue:
- RabbitMQ
- AWS SQS
- Redis Queue

### 8. Retry Logic

Add retry for transient failures:
```java
@Retryable(
    value = {MessagingException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 2000)
)
```

## Security Considerations

### ✅ Do's
- Use App Passwords, not real passwords
- Enable TLS/SSL
- Store credentials in environment variables
- Use dedicated email accounts
- Rotate SMTP passwords regularly
- Monitor for unauthorized use

### ❌ Don'ts
- Don't commit credentials to git
- Don't use personal email for production
- Don't disable TLS/SSL
- Don't ignore bounce notifications
- Don't send passwords in plain text (already encrypted)

## Email Service Provider Comparison

| Provider | Free Tier | Cost | Deliverability | Setup |
|----------|-----------|------|----------------|-------|
| **Gmail** | 500/day | Free | Good | Easy |
| **SendGrid** | 100/day | $15/mo+ | Excellent | Easy |
| **AWS SES** | 62k/mo | $0.10/1000 | Excellent | Medium |
| **Mailgun** | 5k/mo | $35/mo+ | Excellent | Easy |
| **Postmark** | 100/mo | $15/mo+ | Excellent | Easy |

## Advanced Features (Future)

### Email Templates with Thymeleaf
```java
@Service
public class EmailTemplateService {
    @Autowired
    private TemplateEngine templateEngine;
    
    public String generateInvitationEmail(Context context) {
        return templateEngine.process("invitation", context);
    }
}
```

### Email Analytics
- Track open rates
- Track click rates
- Monitor bounce rates
- A/B test templates

### Internationalization
- Multi-language support
- Locale-specific formatting
- Timezone handling

### Rich Content
- Attach PDF guides
- Embed logos/images
- Include QR codes for mobile
- Calendar invites

## Testing in Development

### Use MailHog (SMTP Testing Tool)

1. **Install MailHog:**
```bash
# macOS
brew install mailhog

# Windows
choco install mailhog

# Docker
docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog
```

2. **Configure .env:**
```env
MAIL_HOST=localhost
MAIL_PORT=1025
MAIL_USERNAME=test
MAIL_PASSWORD=test
```

3. **View Emails:**
Open http://localhost:8025 in browser

**Benefits:**
- No real emails sent
- View all emails in web UI
- Test without SMTP credentials
- Perfect for local development

## Support

For email configuration issues:
1. Check application logs
2. Verify SMTP credentials
3. Test with email client
4. Review provider documentation
5. Check firewall/network settings

For email template customization:
1. Edit `EmailService.java`
2. Modify HTML templates
3. Test with different email clients
4. Ensure mobile responsiveness
