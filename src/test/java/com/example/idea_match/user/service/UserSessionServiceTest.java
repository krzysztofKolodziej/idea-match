package com.example.idea_match.user.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.idea_match.user.exceptions.InvalidAuthorizationTokenException;
import com.example.idea_match.user.jwt.JwtUtils;
import com.example.idea_match.user.jwt.RedisTokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserSessionService Unit Tests")
class UserSessionServiceTest {

    @Mock
    private RedisTokenBlacklistService blacklistService;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private UserSessionService userSessionService;

    private static final String VALID_AUTH_HEADER = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsInJvbGUiOiJVU0VSIiwiZXhwIjoxNzM2NjkzMzMwfQ.test";
    private static final String VALID_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsInJvbGUiOiJVU0VSIiwiZXhwIjoxNzM2NjkzMzMwfQ.test";

    @BeforeEach
    void setUp() {
        reset(blacklistService, jwtUtils);
    }

    @Test
    @DisplayName("Should successfully logout user with valid token")
    void shouldLogoutUserWithValidToken() {
        // Given
        Date futureDate = new Date(System.currentTimeMillis() + 3600000); // 1 hour from now
        when(jwtUtils.getExpiration(VALID_TOKEN)).thenReturn(futureDate);

        // When
        userSessionService.logoutUser(VALID_AUTH_HEADER);

        // Then
        verify(jwtUtils).getExpiration(VALID_TOKEN);
        verify(blacklistService).blacklistToken(eq(VALID_TOKEN), anyLong());
    }

    @Test
    @DisplayName("Should blacklist token with zero TTL when token is already expired")
    void shouldBlacklistExpiredTokenWithZeroTtl() {
        // Given
        Date pastDate = new Date(System.currentTimeMillis() - 3600000); // 1 hour ago
        when(jwtUtils.getExpiration(VALID_TOKEN)).thenReturn(pastDate);

        // When
        userSessionService.logoutUser(VALID_AUTH_HEADER);

        // Then
        verify(jwtUtils).getExpiration(VALID_TOKEN);
        verify(blacklistService).blacklistToken(VALID_TOKEN, 0L);
    }

    @Test
    @DisplayName("Should calculate correct TTL for token")
    void shouldCalculateCorrectTtl() {
        // Given
        long currentTime = System.currentTimeMillis();
        long futureTime = currentTime + 7200000; // 2 hours from now
        Date futureDate = new Date(futureTime);
        when(jwtUtils.getExpiration(VALID_TOKEN)).thenReturn(futureDate);

        // When
        userSessionService.logoutUser(VALID_AUTH_HEADER);

        // Then
        verify(blacklistService).blacklistToken(eq(VALID_TOKEN), longThat(ttl -> 
            ttl > 7100000 && ttl <= 7200000 // Allow for small time differences
        ));
    }

    @Test
    @DisplayName("Should throw InvalidAuthorizationToken when header is null")
    void shouldThrowExceptionWhenHeaderIsNull() {
        // When & Then
        assertThatThrownBy(() -> userSessionService.logoutUser(null))
                .isInstanceOf(InvalidAuthorizationTokenException.class)
                .hasMessage("Authorization header is missing or invalid format");

        verifyNoInteractions(jwtUtils, blacklistService);
    }

    @Test
    @DisplayName("Should throw InvalidAuthorizationToken when header is empty")
    void shouldThrowExceptionWhenHeaderIsEmpty() {
        // When & Then
        assertThatThrownBy(() -> userSessionService.logoutUser(""))
                .isInstanceOf(InvalidAuthorizationTokenException.class)
                .hasMessage("Authorization header is missing or invalid format");

        verifyNoInteractions(jwtUtils, blacklistService);
    }

    @Test
    @DisplayName("Should throw InvalidAuthorizationToken when header doesn't start with Bearer")
    void shouldThrowExceptionWhenHeaderDoesNotStartWithBearer() {
        // When & Then
        assertThatThrownBy(() -> userSessionService.logoutUser("Basic dGVzdDp0ZXN0"))
                .isInstanceOf(InvalidAuthorizationTokenException.class)
                .hasMessage("Authorization header is missing or invalid format");

        verifyNoInteractions(jwtUtils, blacklistService);
    }

    @Test
    @DisplayName("Should throw InvalidAuthorizationToken when Bearer token is malformed")
    void shouldThrowExceptionWhenBearerTokenIsMalformed() {
        // When & Then
        assertThatThrownBy(() -> userSessionService.logoutUser("Bearer"))
                .isInstanceOf(InvalidAuthorizationTokenException.class);

        verifyNoInteractions(jwtUtils, blacklistService);
    }

    @Test
    @DisplayName("Should handle JWT verification exception from getExpiration")
    void shouldHandleJwtVerificationException() {
        // Given
        when(jwtUtils.getExpiration(VALID_TOKEN)).thenThrow(new JWTVerificationException("Invalid token"));

        // When & Then
        assertThatThrownBy(() -> userSessionService.logoutUser(VALID_AUTH_HEADER))
                .isInstanceOf(JWTVerificationException.class)
                .hasMessage("Invalid token");

        verify(jwtUtils).getExpiration(VALID_TOKEN);
        verifyNoInteractions(blacklistService);
    }

    @Test
    @DisplayName("Should handle edge case with exactly current time expiration")
    void shouldHandleEdgeCaseWithCurrentTimeExpiration() {
        // Given
        Date currentDate = new Date(System.currentTimeMillis());
        when(jwtUtils.getExpiration(VALID_TOKEN)).thenReturn(currentDate);

        // When
        userSessionService.logoutUser(VALID_AUTH_HEADER);

        // Then
        verify(blacklistService).blacklistToken(eq(VALID_TOKEN), eq(0L));
    }

    @Test
    @DisplayName("Should extract token correctly from Authorization header")
    void shouldExtractTokenCorrectlyFromAuthorizationHeader() {
        // Given
        String customToken = "custom.jwt.token";
        String customAuthHeader = "Bearer " + customToken;
        Date futureDate = new Date(System.currentTimeMillis() + 3600000);
        when(jwtUtils.getExpiration(customToken)).thenReturn(futureDate);

        // When
        userSessionService.logoutUser(customAuthHeader);

        // Then
        verify(jwtUtils).getExpiration(customToken);
        verify(blacklistService).blacklistToken(eq(customToken), anyLong());
    }
}