package com.example.idea_match.user.service;

public interface EmailServiceInterface {
    void sendRegistrationEmail(String recipientEmail, String username, String verificationToken);
}