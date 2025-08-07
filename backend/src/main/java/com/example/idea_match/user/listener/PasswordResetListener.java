package com.example.idea_match.user.listener;

import com.example.idea_match.user.event.PasswordResetRequestedEvent;
import com.example.idea_match.user.event.PasswordResetCompletedEvent;
import com.example.idea_match.user.service.email.PasswordResetEmailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class PasswordResetListener {

    private final PasswordResetEmailService passwordResetEmailService;

    @EventListener
    @Async
    public void handlePasswordResetRequested(PasswordResetRequestedEvent event) {
        log.info("Processing password reset request for email: {}", event.email());
        passwordResetEmailService.sendPasswordResetEmail(event.email(), event.resetToken());
    }

    @EventListener
    @Async
    public void handlePasswordResetCompleted(PasswordResetCompletedEvent event) {
        log.info("Processing password reset completion for email: {}", event.email());
        passwordResetEmailService.sendPasswordResetConfirmationEmail(event.email());
    }
}
