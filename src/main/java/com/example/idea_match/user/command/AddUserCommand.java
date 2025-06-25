package com.example.idea_match.user.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AddUserCommand(
        @NotBlank String firstName,
        @NotBlank String lastname,
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank String phoneNumber,
        String location,
        String aboutMe,
        @NotBlank String password
) {
}
