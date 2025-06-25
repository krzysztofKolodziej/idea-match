package com.example.idea_match.user.exceptions;

public class InvalidJwtTokenException extends RuntimeException{

    public InvalidJwtTokenException(String message) {
        super(message);
    }
}
