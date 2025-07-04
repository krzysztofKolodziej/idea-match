package com.example.idea_match.user.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @NotBlank String usernameOrEmail,
        @NotBlank String password
) {
}
