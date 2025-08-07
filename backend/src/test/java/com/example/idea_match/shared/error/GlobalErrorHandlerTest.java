package com.example.idea_match.shared.error;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GlobalErrorHandlerTest {

    @InjectMocks
    private GlobalErrorHandler globalErrorHandler;

    @Test
    void shouldHandleBadCredentialsException() {
        // given
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");

        // when
        ErrorRespond response = globalErrorHandler.handleAuthenticationErrors(exception);

        // then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.message()).isEqualTo("Invalid credentials");
    }

    @Test
    void shouldHandleDisabledException() {
        // given
        DisabledException exception = new DisabledException("User account is disabled");

        // when
        ErrorRespond response = globalErrorHandler.handleAuthenticationErrors(exception);

        // then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.message()).isEqualTo("Account is disabled");
    }

    @Test
    void shouldHandleGenericRuntimeException() {
        // given
        RuntimeException exception = new RuntimeException("Something went wrong");

        // when
        ErrorRespond response = globalErrorHandler.handleGenericException(exception);

        // then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.message()).isEqualTo("An unexpected error occurred");
    }

    @Test
    void shouldHandleGenericException() {
        // given
        Exception exception = new Exception("Unexpected error");

        // when
        ErrorRespond response = globalErrorHandler.handleGenericException(exception);

        // then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.message()).isEqualTo("An unexpected error occurred");
    }
}