package com.example.idea_match.user.command;

public record AddUserCommand(
        String firstName,
        String lastname,
        String username,
        String email,
        String phoneNumber,
        String location,
        String aboutMe,
        String password
) {
}
