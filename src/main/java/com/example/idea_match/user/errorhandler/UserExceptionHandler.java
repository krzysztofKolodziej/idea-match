package com.example.idea_match.user.errorhandler;

import com.example.idea_match.shared.error.ErrorRespond;
import com.example.idea_match.user.exceptions.BlackListedTokenException;
import com.example.idea_match.user.exceptions.EmailSendingException;
import com.example.idea_match.user.exceptions.ExpiredVerificationTokenException;
import com.example.idea_match.user.exceptions.InvalidJwtTokenException;
import com.example.idea_match.user.exceptions.InvalidVerificationTokenException;
import com.example.idea_match.user.exceptions.UserAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import software.amazon.awssdk.services.ses.model.MessageRejectedException;
import software.amazon.awssdk.services.ses.model.SesException;

import javax.naming.AuthenticationException;

@RestControllerAdvice(basePackages = "com.example.idea_match.user")
@Slf4j
public class UserExceptionHandler {

    @ExceptionHandler(BlackListedTokenException.class)
    public ResponseEntity<ErrorRespond> handleBlacklistedToken(BlackListedTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorRespond(HttpStatus.UNAUTHORIZED, ex.getMessage()));
    }

    @ExceptionHandler(InvalidJwtTokenException.class)
    public ResponseEntity<ErrorRespond> handleInvalidJwt(InvalidJwtTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorRespond(HttpStatus.UNAUTHORIZED, ex.getMessage()));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorRespond> handleUserExists(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorRespond(HttpStatus.CONFLICT, ex.getMessage()));
    }

    @ExceptionHandler(MessageRejectedException.class)
    public ResponseEntity<ErrorRespond> handleMessageRejectedException(MessageRejectedException e) {
        log.error("AWS SES MessageRejectedException: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorRespond(HttpStatus.BAD_REQUEST, "Email service error: " + e.getMessage()));
    }

    @ExceptionHandler(SesException.class)
    public ResponseEntity<ErrorRespond> handleSesException(SesException e) {
        log.error("AWS SES error: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorRespond(HttpStatus.BAD_REQUEST, "Email service error: " + e.getMessage()));
    }

    @ExceptionHandler(EmailSendingException.class)
    public ResponseEntity<ErrorRespond> handleEmailSending(EmailSendingException ex) {
        log.error("Email sending failed: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorRespond(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send email: " + ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthenticationException(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid credentials");
    }

    @ExceptionHandler(InvalidVerificationTokenException.class)
    public ResponseEntity<ErrorRespond> handleInvalidVerificationToken(InvalidVerificationTokenException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorRespond(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(ExpiredVerificationTokenException.class)
    public ResponseEntity<ErrorRespond> handleExpiredVerificationToken(ExpiredVerificationTokenException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorRespond(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }
}
