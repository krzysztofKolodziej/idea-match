package com.example.idea_match.user.command;

import jakarta.validation.constraints.NotBlank;

public record LoginCommand(
        @NotBlank(message = "Username or email is required")
        String usernameOrEmail,
        
        @NotBlank(message = "Password is required")
        String password
) {
}