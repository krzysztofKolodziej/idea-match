package com.example.idea_match.user.event;

import java.time.LocalDateTime;

public record OnRegistrationCompleteEvent(
    String email,
    String username,
    String verificationToken,
    LocalDateTime registeredAt
) {}