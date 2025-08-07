package com.example.idea_match.user.exceptions;

public class BlackListedTokenException extends RuntimeException {

    public BlackListedTokenException(String message) {
        super(message);
    }
}
