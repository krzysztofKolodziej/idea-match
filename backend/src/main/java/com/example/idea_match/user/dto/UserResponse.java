package com.example.idea_match.user.dto;

public record UserResponse(
        Long userId,
        String firstName,
        String lastName,
        String username,
        String email,
        String phoneNumber,
        String profilePictureUrl,
        String location,
        String aboutMe
) {
}
