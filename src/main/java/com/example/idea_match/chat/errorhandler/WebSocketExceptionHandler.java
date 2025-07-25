package com.example.idea_match.chat.errorhandler;

import com.example.idea_match.chat.dto.ErrorMessage;
import com.example.idea_match.chat.exceptions.WebSocketAuthenticationException;
import com.example.idea_match.chat.exceptions.WebSocketTokenBlacklistedException;
import com.example.idea_match.chat.exceptions.WebSocketTokenInvalidException;
import com.example.idea_match.chat.exceptions.WebSocketTokenMissingException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
@Slf4j
public class WebSocketExceptionHandler {

    @MessageExceptionHandler(WebSocketTokenMissingException.class)
    @SendToUser("/queue/errors")
    public ErrorMessage handleTokenMissingException(WebSocketTokenMissingException ex) {
        log.warn("WebSocket authentication failed - token missing");
        return new ErrorMessage("TOKEN_MISSING", "Authentication required. Please provide a valid token.");
    }

    @MessageExceptionHandler(WebSocketTokenBlacklistedException.class)
    @SendToUser("/queue/errors")
    public ErrorMessage handleTokenBlacklistedException(WebSocketTokenBlacklistedException ex) {
        log.warn("WebSocket authentication failed - token blacklisted");
        return new ErrorMessage("TOKEN_BLACKLISTED", "Your session has expired. Please login again.");
    }

    @MessageExceptionHandler(WebSocketTokenInvalidException.class)
    @SendToUser("/queue/errors")
    public ErrorMessage handleTokenInvalidException(WebSocketTokenInvalidException ex) {
        log.warn("WebSocket authentication failed - invalid token");
        return new ErrorMessage("TOKEN_INVALID", "Invalid authentication token. Please login again.");
    }

    @MessageExceptionHandler(WebSocketAuthenticationException.class)
    @SendToUser("/queue/errors")
    public ErrorMessage handleGeneralAuthException(WebSocketAuthenticationException ex) {
        log.error("WebSocket authentication error: {}", ex.getMessage(), ex);
        return new ErrorMessage("AUTH_FAILED", "Authentication failed. Please try again.");
    }

    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    @SendToUser("/queue/errors")
    public ErrorMessage handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());
        return new ErrorMessage("VALIDATION_FAILED", "Invalid message format. Please check your input.");
    }

    @MessageExceptionHandler(ConstraintViolationException.class)
    @SendToUser("/queue/errors")
    public ErrorMessage handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());
        return new ErrorMessage("CONSTRAINT_VIOLATION", "Message validation failed. Please check your input.");
    }

    @MessageExceptionHandler(RuntimeException.class)
    @SendToUser("/queue/errors")
    public ErrorMessage handleRuntimeException(RuntimeException ex) {
        log.error("Runtime error in chat: {}", ex.getMessage(), ex);
        return new ErrorMessage("RUNTIME_ERROR", "Failed to process your message. Please try again.");
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/errors")
    public ErrorMessage handleGeneralException(Exception ex) {
        log.error("Unexpected WebSocket error: {}", ex.getMessage(), ex);
        return new ErrorMessage("UNEXPECTED_ERROR", "An unexpected error occurred. Please try again.");
    }
}