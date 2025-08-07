package com.example.idea_match.user.service.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RegistrationEmailService {
    
    private final EmailService emailService;
    private final String verificationUrl;
    
    public RegistrationEmailService(EmailService emailService,
                                   @Value("${app.verification.url}") String verificationUrl) {
        this.emailService = emailService;
        this.verificationUrl = verificationUrl;
    }
    
    public void sendRegistrationEmail(String recipientEmail, String username, String verificationToken) {
        String subject = "Registration Confirmation - Idea Match";
        String body = buildRegistrationEmailBody(username, verificationToken);
        emailService.sendEmail(recipientEmail, subject, body);
        log.info("Registration email sent to: {}", recipientEmail);
    }
    
    private String buildRegistrationEmailBody(String username, String verificationToken) {
        return String.format(
            """
            Hello %s!
            
            Thank you for registering with Idea Match.
            
            To activate your account, please click the link below:
            %s/verify-email?token=%s
            
            This link is valid for 24 hours.
            
            If you did not register for our service, please ignore this message.
            
            Best regards,
            Idea Match Team""",
            username, verificationUrl, verificationToken
        );
    }
}