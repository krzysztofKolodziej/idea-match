package com.example.idea_match.user.exceptions;

public class ExpiredVerificationTokenException extends RuntimeException {
    public ExpiredVerificationTokenException(String message) {
        super(message);
    }
}