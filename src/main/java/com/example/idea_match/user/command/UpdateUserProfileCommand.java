package com.example.idea_match.user.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateUserProfileCommand(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Pattern(regexp = "^\\+?[1-9]\\d{1,15}$", message = "Invalid phone number format") @NotBlank String phoneNumber,
        String location,
        String aboutMe
) {
}