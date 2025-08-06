package com.example.idea_match.shared.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalErrorHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorRespond handleValidationErrors(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return new ErrorRespond(HttpStatus.BAD_REQUEST, message);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({BadCredentialsException.class, DisabledException.class})
    public ErrorRespond handleAuthenticationErrors(RuntimeException ex) {
        log.warn("Authentication error: {}", ex.getMessage());
        String message = ex instanceof BadCredentialsException ? "Invalid credentials" 
                        : ex instanceof DisabledException ? "Account is disabled"
                        : "Authentication failed";
        return new ErrorRespond(HttpStatus.UNAUTHORIZED, message);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public ErrorRespond handleGenericRuntimeException(RuntimeException ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return new ErrorRespond(HttpStatus.INTERNAL_SERVER_ERROR, "An internal error occurred");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorRespond handleGenericException(Exception ex) {
        log.error("Unexpected exception occurred: {}", ex.getMessage(), ex);
        return new ErrorRespond(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }
}
