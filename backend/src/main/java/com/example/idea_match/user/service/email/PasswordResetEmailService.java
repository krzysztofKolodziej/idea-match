package com.example.idea_match.user.service.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PasswordResetEmailService {
    
    private final EmailService emailService;
    private final String passwordResetUrl;
    
    public PasswordResetEmailService(EmailService emailService,
                                    @Value("${app.password-reset.url}") String passwordResetUrl) {
        this.emailService = emailService;
        this.passwordResetUrl = passwordResetUrl;
    }
    
    public void sendPasswordResetEmail(String recipientEmail, String resetToken) {
        String subject = "Password Reset Request - Idea Match";
        String body = buildPasswordResetEmailBody(resetToken);
        emailService.sendEmail(recipientEmail, subject, body);
        log.info("Password reset email sent to: {}", recipientEmail);
    }
    
    public void sendPasswordResetConfirmationEmail(String recipientEmail) {
        String subject = "Password Reset Confirmation - Idea Match";
        String body = buildPasswordResetConfirmationBody();
        emailService.sendEmail(recipientEmail, subject, body);
        log.info("Password reset confirmation email sent to: {}", recipientEmail);
    }
    
    private String buildPasswordResetEmailBody(String resetToken) {
        return String.format(
            """
            Hello!
            
            You have requested to reset your password for your Idea Match account.
            
            Click the link below to reset your password:
            %s/reset-password?token=%s
            
            This link is valid for 1 hour.
            
            If you did not request a password reset, please ignore this message.
            
            Best regards,
            Idea Match Team""",
            passwordResetUrl, resetToken
        );
    }
    
    private String buildPasswordResetConfirmationBody() {
        return """
            Hello!
            
            Your password has been successfully reset for your Idea Match account.
            
            If you did not perform this action, please contact our support team immediately.
            
            Best regards,
            Idea Match Team""";
    }
}