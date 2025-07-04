package com.example.idea_match.user.controller;

import com.example.idea_match.user.service.HandlerVerificationToken;
import com.example.idea_match.user.service.UserRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserRegistrationControllerVerificationTest {

    @Mock
    private HandlerVerificationToken handlerVerificationToken;

    @Mock
    private UserRegistrationService userRegistrationService;

    @InjectMocks
    private UserRegistrationController userRegistrationController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userRegistrationController).build();
    }

    @Test
    void shouldReturnSuccessForValidToken() throws Exception {
        // given
        String validToken = "valid-token-123";
        when(handlerVerificationToken.validateVerificationToken(validToken))
                .thenReturn("valid");

        // when & then
        mockMvc.perform(get("/verify-email")
                        .param("token", validToken))
                .andExpect(status().isFound())
                .andExpect(content().string("Your account has been verified successfully."));
    }

    @Test
    void shouldReturnGoneForExpiredToken() throws Exception {
        // given
        String expiredToken = "expired-token-123";
        when(handlerVerificationToken.validateVerificationToken(expiredToken))
                .thenReturn("expired");

        // when & then
        mockMvc.perform(get("/verify-email")
                        .param("token", expiredToken))
                .andExpect(status().isGone())
                .andExpect(content().string("Verification token has been expired."));
    }

    @Test
    void shouldReturnNotFoundForInvalidToken() throws Exception {
        // given
        String invalidToken = "invalid-token-123";
        when(handlerVerificationToken.validateVerificationToken(invalidToken))
                .thenReturn("invalid");

        // when & then
        mockMvc.perform(get("/verify-email")
                        .param("token", invalidToken))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Invalid verification token."));
    }

    @Test
    void shouldReturnNotFoundForUnknownTokenResult() throws Exception {
        // given
        String unknownToken = "unknown-token-123";
        when(handlerVerificationToken.validateVerificationToken(unknownToken))
                .thenReturn("unknown"); // unexpected result

        // when & then
        mockMvc.perform(get("/verify-email")
                        .param("token", unknownToken))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Invalid verification token."));
    }

    @Test
    void shouldReturnBadRequestWhenTokenParameterIsMissing() throws Exception {
        // when & then
        mockMvc.perform(get("/verify-email"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleEmptyTokenParameter() throws Exception {
        // given
        String emptyToken = "";
        when(handlerVerificationToken.validateVerificationToken(emptyToken))
                .thenReturn("invalid");

        // when & then
        mockMvc.perform(get("/verify-email")
                        .param("token", emptyToken))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Invalid verification token."));
    }

    @Test
    void shouldHandleWhitespaceOnlyToken() throws Exception {
        // given
        String whitespaceToken = "   ";
        when(handlerVerificationToken.validateVerificationToken(whitespaceToken))
                .thenReturn("invalid");

        // when & then
        mockMvc.perform(get("/verify-email")
                        .param("token", whitespaceToken))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Invalid verification token."));
    }

    @Test
    void shouldHandleLongTokenString() throws Exception {
        // given
        String longToken = "a".repeat(1000);
        when(handlerVerificationToken.validateVerificationToken(longToken))
                .thenReturn("invalid");

        // when & then
        mockMvc.perform(get("/verify-email")
                        .param("token", longToken))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Invalid verification token."));
    }

    @Test
    void shouldHandleTokenWithSpecialCharacters() throws Exception {
        // given
        String specialCharToken = "token-with-!@#$%^&*()";
        when(handlerVerificationToken.validateVerificationToken(specialCharToken))
                .thenReturn("invalid");

        // when & then
        mockMvc.perform(get("/verify-email")
                        .param("token", specialCharToken))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Invalid verification token."));
    }

    @Test
    void shouldHandleNullTokenResult() throws Exception {
        // given
        String nullResultToken = "null-result-token";
        when(handlerVerificationToken.validateVerificationToken(nullResultToken))
                .thenReturn(null);

        // when & then
        mockMvc.perform(get("/verify-email")
                        .param("token", nullResultToken))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Invalid verification token."));
    }
}