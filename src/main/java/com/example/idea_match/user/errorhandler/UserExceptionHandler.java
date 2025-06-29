package com.example.idea_match.user.errorhandler;

import com.example.idea_match.shared.error.ErrorRespond;
import com.example.idea_match.user.exceptions.BlackListedTokenException;
import com.example.idea_match.user.exceptions.EmailSendingException;
import com.example.idea_match.user.exceptions.InvalidJwtTokenException;
import com.example.idea_match.user.exceptions.UserAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import software.amazon.awssdk.services.ses.model.MessageRejectedException;
import software.amazon.awssdk.services.ses.model.SesException;

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

}
