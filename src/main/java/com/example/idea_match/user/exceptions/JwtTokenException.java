package com.example.idea_match.user.exceptions;

public class JwtTokenException extends RuntimeException {

    public JwtTokenException(String message) {
        super(message);
    }
}
