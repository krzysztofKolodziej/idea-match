package com.example.idea_match.user.exceptions;

public class UsernameOrEmailNotFoundException extends RuntimeException {

    public UsernameOrEmailNotFoundException(String message) {
        super(message);
    }
}
