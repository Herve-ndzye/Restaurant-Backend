package com.mavic.backend.common.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@restaurant.com}")
    private String fromEmail;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    @Value("${app.name:Restaurant Management System}")
    private String appName;
    
    @Value("${spring.mail.enabled:false}")
    private boolean emailEnabled;
    
    /**
     * Send admin invitation email with temporary credentials
     */
    @Async
    public void sendAdminInvitationEmail(
            String toEmail, 
            String adminName, 
            String username, 
            String temporaryPassword,
            String invitedByAdmin) {
        
        if (!emailEnabled || mailSender == null) {
            log.warn("Email service not configured. Skipping invitation email to: {}", toEmail);
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to " + appName + " - Admin Account Created");
            
            String htmlContent = buildAdminInvitationEmailTemplate(
                    adminName, 
                    username, 
                    temporaryPassword, 
                    invitedByAdmin
            );
            
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Admin invitation email sent successfully to: {}", toEmail);
            
        } catch (MessagingException e) {
            log.error("Failed to send admin invitation email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send invitation email: " + e.getMessage());
        }
    }
    
    /**
     * Build HTML email template for admin invitation
     */
    private String buildAdminInvitationEmailTemplate(
            String adminName, 
            String username, 
            String temporaryPassword,
            String invitedByAdmin) {
        
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .header {
                            background-color: #4CAF50;
                            color: white;
                            padding: 20px;
                            text-align: center;
                            border-radius: 5px 5px 0 0;
                        }
                        .content {
                            background-color: #f9f9f9;
                            padding: 30px;
                            border: 1px solid #ddd;
                            border-radius: 0 0 5px 5px;
                        }
                        .credentials-box {
                            background-color: #fff;
                            border: 2px solid #4CAF50;
                            border-radius: 5px;
                            padding: 20px;
                            margin: 20px 0;
                        }
                        .credentials-box h3 {
                            margin-top: 0;
                            color: #4CAF50;
                        }
                        .credential-item {
                            margin: 15px 0;
                            padding: 10px;
                            background-color: #f5f5f5;
                            border-left: 4px solid #4CAF50;
                        }
                        .credential-label {
                            font-weight: bold;
                            color: #666;
                            font-size: 12px;
                            text-transform: uppercase;
                        }
                        .credential-value {
                            font-size: 16px;
                            color: #333;
                            font-family: 'Courier New', monospace;
                            margin-top: 5px;
                        }
                        .button {
                            display: inline-block;
                            padding: 12px 30px;
                            background-color: #4CAF50;
                            color: white;
                            text-decoration: none;
                            border-radius: 5px;
                            margin: 20px 0;
                            font-weight: bold;
                        }
                        .warning {
                            background-color: #fff3cd;
                            border: 1px solid #ffc107;
                            border-radius: 5px;
                            padding: 15px;
                            margin: 20px 0;
                        }
                        .warning-icon {
                            color: #ff9800;
                            font-size: 20px;
                            margin-right: 10px;
                        }
                        .footer {
                            text-align: center;
                            margin-top: 30px;
                            padding-top: 20px;
                            border-top: 1px solid #ddd;
                            color: #666;
                            font-size: 12px;
                        }
                        .steps {
                            background-color: #fff;
                            padding: 15px;
                            border-radius: 5px;
                            margin: 20px 0;
                        }
                        .step {
                            margin: 10px 0;
                            padding-left: 30px;
                            position: relative;
                        }
                        .step::before {
                            content: "→";
                            position: absolute;
                            left: 10px;
                            color: #4CAF50;
                            font-weight: bold;
                        }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h1>🎉 Welcome to %s!</h1>
                    </div>
                    
                    <div class="content">
                        <p>Hi <strong>%s</strong>,</p>
                        
                        <p>You've been invited by <strong>%s</strong> to join as a Restaurant Administrator.</p>
                        
                        <div class="credentials-box">
                            <h3>🔑 Your Login Credentials</h3>
                            
                            <div class="credential-item">
                                <div class="credential-label">Username</div>
                                <div class="credential-value">%s</div>
                            </div>
                            
                            <div class="credential-item">
                                <div class="credential-label">Temporary Password</div>
                                <div class="credential-value">%s</div>
                            </div>
                        </div>
                        
                        <div class="warning">
                            <span class="warning-icon">⚠️</span>
                            <strong>Important Security Notice:</strong>
                            <ul style="margin: 10px 0;">
                                <li>This is a temporary password</li>
                                <li>You <strong>must</strong> change it on your first login</li>
                                <li>Never share your password with anyone</li>
                                <li>This email contains sensitive information - please delete it after changing your password</li>
                            </ul>
                        </div>
                        
                        <div style="text-align: center;">
                            <a href="%s/login" class="button">Login Now</a>
                        </div>
                        
                        <div class="steps">
                            <h3>📝 Next Steps:</h3>
                            <div class="step">Visit the login page</div>
                            <div class="step">Enter your username and temporary password</div>
                            <div class="step">You'll be prompted to change your password</div>
                            <div class="step">Choose a strong, secure password</div>
                            <div class="step">Start managing your restaurant!</div>
                        </div>
                        
                        <p style="margin-top: 30px;">
                            <strong>Need help?</strong><br>
                            If you have any questions or didn't request this account, 
                            please contact your system administrator immediately.
                        </p>
                        
                        <div class="footer">
                            <p>This is an automated message from %s.</p>
                            <p>Please do not reply to this email.</p>
                            <p>&copy; 2026 %s. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                appName,           // Header title
                adminName,         // Greeting
                invitedByAdmin,    // Who invited
                username,          // Username credential
                temporaryPassword, // Password credential
                frontendUrl,       // Login button URL
                appName,           // Footer app name 1
                appName            // Footer app name 2
        );
    }
    
    /**
     * Send password change confirmation email
     */
    @Async
    public void sendPasswordChangedEmail(String toEmail, String username) {
        if (!emailEnabled || mailSender == null) {
            log.warn("Email service not configured. Skipping password change confirmation to: {}", toEmail);
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Password Changed Successfully - " + appName);
            
            String htmlContent = buildPasswordChangedEmailTemplate(username);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Password change confirmation email sent to: {}", toEmail);
            
        } catch (MessagingException e) {
            log.error("Failed to send password change confirmation email to: {}", toEmail, e);
            // Don't throw exception - password was already changed successfully
        }
    }
    
    /**
     * Build HTML email template for password change confirmation
     */
    private String buildPasswordChangedEmailTemplate(String username) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                        .content { background-color: #f9f9f9; padding: 30px; border: 1px solid #ddd; border-radius: 0 0 5px 5px; }
                        .success-box { background-color: #d4edda; border: 1px solid #c3e6cb; border-radius: 5px; padding: 15px; margin: 20px 0; }
                        .footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h1>✅ Password Changed Successfully</h1>
                    </div>
                    <div class="content">
                        <p>Hi <strong>%s</strong>,</p>
                        <div class="success-box">
                            <p><strong>✓ Your password has been changed successfully.</strong></p>
                            <p>Time: %s</p>
                        </div>
                        <p>If you did not make this change, please contact your system administrator immediately.</p>
                        <div class="footer">
                            <p>&copy; 2026 %s. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                username,
                java.time.LocalDateTime.now().toString(),
                appName
        );
    }
}
