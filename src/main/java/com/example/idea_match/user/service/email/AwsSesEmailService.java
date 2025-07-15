package com.example.idea_match.user.service.email;

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
public class AwsSesEmailService implements EmailService {

    private final SesClient sesClient;
    private final String fromEmail;

    public AwsSesEmailService(@Value("${aws.ses.access-key}") String accessKey,
                             @Value("${aws.ses.secret-key}") String secretKey,
                             @Value("${aws.ses.region}") String region,
                             @Value("${aws.ses.from-email}") String fromEmail) {
        
        this.sesClient = SesClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of(region))
                .build();
        
        this.fromEmail = fromEmail;
    }

    @Override
    public void sendEmail(String recipientEmail, String subject, String body) {
        try {
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
            log.info("Email sent successfully to: {}. MessageId: {}", recipientEmail, result.messageId());
            
        } catch (Exception e) {
            log.error("Failed to send email to: {}", recipientEmail, e);
            throw new EmailSendingException("Failed to send email to: " + recipientEmail, e);
        }
    }
}