package com.example.idea_match.user.exceptions;

public class IncorrectUserPasswordException extends RuntimeException {
    public IncorrectUserPasswordException(String message) {
        super(message);
    }
}
