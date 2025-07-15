package com.example.idea_match.user.controller;

import com.example.idea_match.user.errorhandler.UserExceptionHandler;
import com.example.idea_match.user.exceptions.ExpiredVerificationTokenException;
import com.example.idea_match.user.exceptions.InvalidVerificationTokenException;
import com.example.idea_match.user.service.TokenService;
import com.example.idea_match.user.service.UserRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserRegistrationControllerVerificationTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private UserRegistrationService userRegistrationService;

    @InjectMocks
    private UserRegistrationController userRegistrationController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userRegistrationController)
                .setControllerAdvice(new UserExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnOkForValidToken() throws Exception {
        // given
        String validToken = "valid-token-123";
        doNothing().when(tokenService).validateVerificationToken(validToken);

        // when & then
        mockMvc.perform(get("/api/verify-email")
                        .param("token", validToken))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnBadRequestForExpiredToken() throws Exception {
        // given
        String expiredToken = "expired-token-123";
        doThrow(new ExpiredVerificationTokenException("Verification token has expired"))
                .when(tokenService).validateVerificationToken(expiredToken);

        // when & then
        mockMvc.perform(get("/api/verify-email")
                        .param("token", expiredToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForInvalidToken() throws Exception {
        // given
        String invalidToken = "invalid-token-123";
        doThrow(new InvalidVerificationTokenException("Invalid verification token"))
                .when(tokenService).validateVerificationToken(invalidToken);

        // when & then
        mockMvc.perform(get("/api/verify-email")
                        .param("token", invalidToken))
                .andExpect(status().isBadRequest());
    }
}