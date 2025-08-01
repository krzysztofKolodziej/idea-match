package com.example.idea_match.user.service;

import com.example.idea_match.shared.security.TokenBlacklistService;
import com.example.idea_match.shared.security.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserSessionService Unit Tests")
class UserSessionServiceTest {

    @Mock
    private TokenBlacklistService blacklistService;

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









}