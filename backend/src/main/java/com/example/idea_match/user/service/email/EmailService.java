package com.example.idea_match.user.service.email;

public interface EmailService {
    void sendEmail(String recipientEmail, String subject, String body);
}