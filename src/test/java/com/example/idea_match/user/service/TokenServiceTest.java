package com.example.idea_match.user.service;

import com.example.idea_match.user.exceptions.ExpiredVerificationTokenException;
import com.example.idea_match.user.exceptions.InvalidTokenException;
import com.example.idea_match.user.exceptions.InvalidVerificationTokenException;
import com.example.idea_match.user.model.Role;
import com.example.idea_match.user.model.User;
import com.example.idea_match.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TokenService tokenService;

    private User testUser;
    private LocalDateTime expirationTime;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .email("john@example.com")
                .phoneNumber("+48123456789")
                .password("password123")
                .role(Role.USER)
                .enabled(false)
                .build();

        expirationTime = LocalDateTime.now().plusHours(24);
    }

    @Test
    void shouldCreateVerificationTokenSuccessfully() {
        // when
        User result = tokenService.createVerificationToken(testUser, expirationTime);

        // then
        assertThat(result.getVerificationToken()).isNotNull();
        assertThat(result.getVerificationToken()).isNotEmpty();
        assertThat(result.getTokenExpirationTime()).isEqualTo(expirationTime);
        assertThat(result.getVerificationToken()).hasSize(36);
    }

    @Test
    void shouldValidateTokenSuccessfully() {
        // given
        String validToken = "valid-token-123";
        testUser.setVerificationToken(validToken);
        testUser.setTokenExpirationTime(LocalDateTime.now().plusHours(1));
        when(userRepository.findByVerificationToken(validToken)).thenReturn(Optional.of(testUser));

        // when
        tokenService.validateVerificationToken(validToken);

        // then - no exception thrown
        // Verify user is enabled and saved
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().isEnabled()).isTrue();
    }

    @Test
    void shouldThrowExpiredExceptionForExpiredToken() {
        // given
        String expiredToken = "expired-token-123";
        testUser.setVerificationToken(expiredToken);
        testUser.setTokenExpirationTime(LocalDateTime.now().minusHours(1)); // expired
        when(userRepository.findByVerificationToken(expiredToken)).thenReturn(Optional.of(testUser));

        // when & then
        assertThatThrownBy(() -> tokenService.validateVerificationToken(expiredToken))
                .isInstanceOf(ExpiredVerificationTokenException.class)
                .hasMessage("Verification token has expired");
        
        // Verify user is not saved when token is expired
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowInvalidExceptionForNonExistentToken() {
        // given
        String invalidToken = "non-existent-token";
        when(userRepository.findByVerificationToken(invalidToken)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tokenService.validateVerificationToken(invalidToken))
                .isInstanceOf(InvalidVerificationTokenException.class)
                .hasMessage("Invalid verification token");
        
        // Verify no save operation occurs
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldValidatePasswordResetTokenSuccessfully() {
        // given
        String validToken = "valid-reset-token";
        testUser.setPasswordResetToken(validToken);
        testUser.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
        when(userRepository.findByPasswordResetToken(validToken)).thenReturn(Optional.of(testUser));

        // when
        tokenService.validatePasswordResetToken(validToken);

        // then - no exception thrown
    }

    @Test
    void shouldThrowInvalidExceptionForExpiredPasswordResetToken() {
        // given
        String expiredToken = "expired-reset-token";
        testUser.setPasswordResetToken(expiredToken);
        testUser.setPasswordResetTokenExpiry(LocalDateTime.now().minusHours(1)); // expired
        when(userRepository.findByPasswordResetToken(expiredToken)).thenReturn(Optional.of(testUser));

        // when & then
        assertThatThrownBy(() -> tokenService.validatePasswordResetToken(expiredToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Reset token has expired");
    }

    @Test
    void shouldThrowInvalidExceptionForNonExistentPasswordResetToken() {
        // given
        String invalidToken = "non-existent-reset-token";
        when(userRepository.findByPasswordResetToken(invalidToken)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tokenService.validatePasswordResetToken(invalidToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Invalid or expired reset token");
    }

    @Test
    void shouldCreateUniqueTokensForMultipleInvocations() {
        // given
        User secondUser = User.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .username("janesmith")
                .email("jane@example.com")
                .phoneNumber("+48987654321")
                .password("password456")
                .role(Role.USER)
                .enabled(false)
                .build();

        // when
        User firstResult = tokenService.createVerificationToken(testUser, expirationTime);
        User secondResult = tokenService.createVerificationToken(secondUser, expirationTime);

        // then
        assertThat(firstResult.getVerificationToken()).isNotEqualTo(secondResult.getVerificationToken());
    }

    @Test
    void shouldPreserveUserDataWhenCreatingToken() {
        // when
        User result = tokenService.createVerificationToken(testUser, expirationTime);

        // then
        assertThat(result.getId()).isEqualTo(testUser.getId());
        assertThat(result.getFirstName()).isEqualTo(testUser.getFirstName());
        assertThat(result.getLastName()).isEqualTo(testUser.getLastName());
        assertThat(result.getUsername()).isEqualTo(testUser.getUsername());
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(result.getPhoneNumber()).isEqualTo(testUser.getPhoneNumber());
        assertThat(result.getPassword()).isEqualTo(testUser.getPassword());
        assertThat(result.getRole()).isEqualTo(testUser.getRole());
        assertThat(result.isEnabled()).isEqualTo(testUser.isEnabled());
    }

    @Test
    void shouldSetCorrectExpirationTime() {
        // given
        LocalDateTime customExpirationTime = LocalDateTime.now().plusDays(7);

        // when
        User result = tokenService.createVerificationToken(testUser, customExpirationTime);

        // then
        assertThat(result.getTokenExpirationTime()).isEqualTo(customExpirationTime);
    }
}