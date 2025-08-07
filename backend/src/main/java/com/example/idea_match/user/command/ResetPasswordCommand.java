package com.example.idea_match.user.command;

import com.example.idea_match.user.command.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordCommand(
        @NotBlank(message = "Token is required")
        String token,
        
        @NotBlank(message = "New password is required")
        @ValidPassword
        String newPassword
) {
}