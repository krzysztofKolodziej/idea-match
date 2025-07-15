package com.example.idea_match.user.event;

import java.time.LocalDateTime;


public record PasswordResetRequestedEvent(
        String email,
        String resetToken,
        LocalDateTime requestedAt) {
}