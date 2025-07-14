package com.example.idea_match.user.exceptions;

public class IncorrectUserPassword extends RuntimeException {
    public IncorrectUserPassword(String message) {
        super(message);
    }
}
