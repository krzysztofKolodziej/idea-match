package com.example.idea_match.user.errorhandler;

import com.example.idea_match.shared.error.ErrorRespond;
import com.example.idea_match.user.exceptions.BlackListedTokenException;
import com.example.idea_match.user.exceptions.EmailSendingException;
import com.example.idea_match.user.exceptions.InvalidJwtTokenException;
import com.example.idea_match.user.exceptions.UserAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import software.amazon.awssdk.services.ses.model.MessageRejectedException;
import software.amazon.awssdk.services.ses.model.SesException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UserExceptionHandlerTest {

    @InjectMocks
    private UserExceptionHandler userExceptionHandler;

    @Test
    void shouldHandleBlackListedTokenException() {
        // given
        BlackListedTokenException exception = new BlackListedTokenException("Token is blacklisted");

        // when
        ErrorRespond response = userExceptionHandler.handleUnauthorized(exception);

        // then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.message()).isEqualTo("Token is blacklisted");
    }

    @Test
    void shouldHandleInvalidJwtTokenException() {
        // given
        InvalidJwtTokenException exception = new InvalidJwtTokenException("JWT token is invalid");

        // when
        ErrorRespond response = userExceptionHandler.handleUnauthorized(exception);

        // then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.message()).isEqualTo("JWT token is invalid");
    }


    @Test
    void shouldHandleUserAlreadyExistsException() {
        // given
        UserAlreadyExistsException exception = new UserAlreadyExistsException("User already exists");

        // when
        ErrorRespond response = userExceptionHandler.handleUserExists(exception);

        // then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.message()).isEqualTo("User already exists");
    }

    @Test
    void shouldHandleMessageRejectedException() {
        // given
        MessageRejectedException exception = MessageRejectedException.builder()
                .message("Email was rejected by SES")
                .build();

        // when
        ErrorRespond response = userExceptionHandler.handleEmailErrors(exception);

        // then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.message()).isEqualTo("Email service error: Email was rejected by SES");
    }

    @Test
    void shouldHandleSesException() {
        // given
        SesException exception = (SesException) SesException.builder()
                .message("SES service unavailable")
                .build();

        // when
        ErrorRespond response = userExceptionHandler.handleEmailErrors(exception);

        // then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.message()).isEqualTo("Email service error: SES service unavailable");
    }

    @Test
    void shouldHandleEmailSendingException() {
        // given
        EmailSendingException exception = new EmailSendingException("Failed to send email to user@example.com", new Exception());

        // when
        ErrorRespond response = userExceptionHandler.handleEmailSending(exception);

        // then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.message()).isEqualTo("Failed to send email: Failed to send email to user@example.com");
    }

    @Test
    void shouldHandleEmailSendingExceptionWithCause() {
        // given
        RuntimeException cause = new RuntimeException("Network timeout");
        EmailSendingException exception = new EmailSendingException("Email sending failed", cause);

        // when
        ErrorRespond response = userExceptionHandler.handleEmailSending(exception);

        // then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.message()).isEqualTo("Failed to send email: Email sending failed");
    }

    @Test
    void shouldHandleExceptionsWithNullMessages() {
        // given
        UserAlreadyExistsException exception = new UserAlreadyExistsException(null);

        // when
        ErrorRespond response = userExceptionHandler.handleUserExists(exception);

        // then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.message()).isNull();
    }

    @Test
    void shouldHandleExceptionsWithEmptyMessages() {
        // given
        InvalidJwtTokenException exception = new InvalidJwtTokenException("");

        // when
        ErrorRespond response = userExceptionHandler.handleUnauthorized(exception);

        // then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.message()).isEmpty();
    }

    @Test
    void shouldHandleMessageRejectedExceptionWithNullMessage() {
        // given
        MessageRejectedException exception = MessageRejectedException.builder()
                .message(null)
                .build();

        // when
        ErrorRespond response = userExceptionHandler.handleEmailErrors(exception);

        // then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.message()).isEqualTo("Email service error: null");
    }

    @Test
    void shouldHandleSesExceptionWithDetailedMessage() {
        // given
        SesException exception = (SesException) SesException.builder()
                .message("The request signature we calculated does not match the signature you provided")
                .build();

        // when
        ErrorRespond response = userExceptionHandler.handleEmailErrors(exception);

        // then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.message())
                .isEqualTo("Email service error: The request signature we calculated does not match the signature you provided");
    }
}