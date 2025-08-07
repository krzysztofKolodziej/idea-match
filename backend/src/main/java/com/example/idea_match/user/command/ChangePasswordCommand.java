package com.example.idea_match.user.command;

import com.example.idea_match.user.command.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;

public record ChangePasswordCommand(

        @NotBlank(message = "Old password is required")
        String oldPassword,

        @NotBlank(message = "New password is required")
        @ValidPassword
        String newPassword
) {
}
