package com.example.idea_match.user.command;

import com.example.idea_match.user.command.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddUserCommand(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String username,
        @NotBlank @Email String email,
        @Pattern(regexp = "^\\+?[1-9]\\d{1,15}$", message = "Invalid phone number format") @NotBlank String phoneNumber,
        String location,
        String aboutMe,
        @ValidPassword String password
) {
}
