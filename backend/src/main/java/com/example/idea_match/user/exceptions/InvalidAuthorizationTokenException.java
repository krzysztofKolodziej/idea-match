package com.example.idea_match.user.exceptions;

public class InvalidAuthorizationTokenException extends RuntimeException {

    public InvalidAuthorizationTokenException(String message) {
        super(message);
    }
}

