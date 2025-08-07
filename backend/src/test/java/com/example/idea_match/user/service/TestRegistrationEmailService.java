package com.example.idea_match.user.service;

import com.example.idea_match.user.service.email.RegistrationEmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Primary
@Profile("test")
@Slf4j
public class TestRegistrationEmailService extends RegistrationEmailService {

    public TestRegistrationEmailService() {
        super(null, "http://localhost:8080/verify");
    }

    @Override
    public void sendRegistrationEmail(String recipientEmail, String username, String verificationToken) {
        log.info("TEST MODE: Would send email to {} with token {}", recipientEmail, verificationToken);
        // Do nothing - just log instead of calling AWS SES
    }
}