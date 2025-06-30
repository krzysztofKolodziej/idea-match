package com.example.idea_match.user.service;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;
import com.example.idea_match.user.exceptions.EmailSendingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService implements EmailServiceInterface {

    private final SesClient sesClient;
    private final String fromEmail;
    private final String verificationUrl;

    public EmailService(@Value("${aws.ses.access-key}") String accessKey,
                       @Value("${aws.ses.secret-key}") String secretKey,
                       @Value("${aws.ses.region}") String region,
                       @Value("${aws.ses.from-email}") String fromEmail,
                       @Value("${app.verification.url}") String verificationUrl) {
        
        this.sesClient = SesClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of(region))
                .build();
        
        this.fromEmail = fromEmail;
        this.verificationUrl = verificationUrl;
    }

    public void sendRegistrationEmail(String recipientEmail, String username, String verificationToken) {
        try {
            String subject = "Registration Confirmation - Idea Match";
            String body = buildRegistrationEmailBody(username, verificationToken);
            
            SendEmailRequest request = SendEmailRequest.builder()
                .source(fromEmail)
                .destination(Destination.builder()
                    .toAddresses(recipientEmail)
                    .build())
                .message(Message.builder()
                    .subject(Content.builder().data(subject).build())
                    .body(Body.builder()
                        .text(Content.builder().data(body).build())
                        .build())
                    .build())
                .build();
            
            SendEmailResponse result = sesClient.sendEmail(request);
            
            log.info("Registration email sent successfully to: {}. MessageId: {}", 
                recipientEmail, result.messageId());
            
        } catch (Exception e) {
            log.error("Failed to send registration email to: {}", recipientEmail, e);
            throw new EmailSendingException("Failed to send registration email to: " + recipientEmail, e);
        }
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