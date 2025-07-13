package com.example.idea_match.user.exceptions;

public class InvalidAuthorizationToken extends RuntimeException {

    public InvalidAuthorizationToken(String message) {
        super(message);
    }
}

