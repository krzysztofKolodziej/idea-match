package com.example.idea_match.user.event;

import java.time.LocalDateTime;

public record PasswordResetCompletedEvent(
        String email,
        LocalDateTime completedAt) {
}