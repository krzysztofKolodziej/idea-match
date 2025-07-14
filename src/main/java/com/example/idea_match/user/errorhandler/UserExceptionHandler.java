package com.example.idea_match.user.errorhandler;

import com.example.idea_match.shared.error.ErrorRespond;
import com.example.idea_match.user.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import software.amazon.awssdk.services.ses.model.MessageRejectedException;
import software.amazon.awssdk.services.ses.model.SesException;

import java.util.stream.Collectors;

@RestControllerAdvice(basePackages = "com.example.idea_match.user")
@Slf4j
public class UserExceptionHandler {

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({BlackListedTokenException.class, InvalidJwtTokenException.class, BadCredentialsException.class,
            DisabledException.class, IncorrectUserPasswordException.class})
    public ErrorRespond handleUnauthorized(RuntimeException ex) {
        String message = ex instanceof BadCredentialsException ? "Invalid credentials" 
                        : ex instanceof DisabledException ? "Account is disabled"
                        : ex.getMessage();
        return new ErrorRespond(HttpStatus.UNAUTHORIZED, message);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ErrorRespond handleUserExists(UserAlreadyExistsException ex) {
        return new ErrorRespond(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({InvalidVerificationTokenException.class, ExpiredVerificationTokenException.class, InvalidAuthorizationTokenException.class})
    public ErrorRespond handleBadRequest(RuntimeException ex) {
        return new ErrorRespond(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MessageRejectedException.class, SesException.class})
    public ErrorRespond handleEmailErrors(Exception ex) {
        log.error("Email service error: {}", ex.getMessage(), ex);
        return new ErrorRespond(HttpStatus.BAD_REQUEST, "Email service error: " + ex.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(EmailSendingException.class)
    public ErrorRespond handleEmailSending(EmailSendingException ex) {
        log.error("Email sending failed: {}", ex.getMessage(), ex);
        return new ErrorRespond(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send email: " + ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorRespond handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return new ErrorRespond(HttpStatus.BAD_REQUEST, message);
    }


}
