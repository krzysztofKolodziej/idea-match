package com.example.idea_match.user.service;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HandlerVerificationTokenTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private HandlerVerificationToken handlerVerificationToken;

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
                .password("encodedPassword")
                .enabled(false)
                .role(Role.USER)
                .build();

        expirationTime = LocalDateTime.now().plusHours(24);
    }

    @Test
    void shouldCreateVerificationTokenSuccessfully() {
        // when
        User result = handlerVerificationToken.createVerificationToken(testUser, expirationTime);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getVerificationToken()).isNotNull();
        assertThat(result.getVerificationToken()).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        assertThat(result.getTokenExpirationTime()).isEqualTo(expirationTime);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("johndoe");
    }

    @Test
    void shouldGenerateUniqueTokensForMultipleCalls() {
        // given
        User testUser2 = User.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Doe")
                .username("janedoe")
                .email("jane@example.com")
                .phoneNumber("+48987654321")
                .password("encodedPassword")
                .enabled(false)
                .role(Role.USER)
                .build();

        // when
        User result1 = handlerVerificationToken.createVerificationToken(testUser, expirationTime);
        User result2 = handlerVerificationToken.createVerificationToken(testUser2, expirationTime);

        // then
        assertThat(result1.getVerificationToken()).isNotEqualTo(result2.getVerificationToken());
        assertThat(result1.getVerificationToken()).isNotNull();
        assertThat(result2.getVerificationToken()).isNotNull();
    }

    @Test
    void shouldSetExpirationTimeCorrectly() {
        // given
        LocalDateTime customExpirationTime = LocalDateTime.of(2024, 12, 31, 23, 59, 59);

        // when
        User result = handlerVerificationToken.createVerificationToken(testUser, customExpirationTime);

        // then
        assertThat(result.getTokenExpirationTime()).isEqualTo(customExpirationTime);
    }

    @Test
    void shouldValidateTokenSuccessfullyForValidToken() {
        // given
        String validToken = "valid-token-123";
        User userWithToken = User.builder()
                .verificationToken(validToken)
                .tokenExpirationTime(LocalDateTime.now().plusHours(1))
                .build();

        when(userRepository.findByVerificationToken(validToken))
                .thenReturn(Optional.of(userWithToken));

        // when
        String result = handlerVerificationToken.validateVerificationToken(validToken);

        // then
        assertThat(result).isEqualTo("valid");
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.isEnabled()).isTrue();
        assertThat(savedUser.getVerificationToken()).isEqualTo(validToken);
    }

    @Test
    void shouldReturnInvalidForNonExistentToken() {
        // given
        String invalidToken = "non-existent-token";
        when(userRepository.findByVerificationToken(invalidToken))
                .thenReturn(Optional.empty());

        // when
        String result = handlerVerificationToken.validateVerificationToken(invalidToken);

        // then
        assertThat(result).isEqualTo("invalid");
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldReturnExpiredForExpiredToken() {
        // given
        String expiredToken = "expired-token-123";
        User userWithExpiredToken = User.builder()
                .verificationToken(expiredToken)
                .tokenExpirationTime(LocalDateTime.now().minusHours(1)) // expired 1 hour ago
                .build();

        when(userRepository.findByVerificationToken(expiredToken))
                .thenReturn(Optional.of(userWithExpiredToken));

        // when
        String result = handlerVerificationToken.validateVerificationToken(expiredToken);

        // then
        assertThat(result).isEqualTo("expired");
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldReturnExpiredForTokenExpiringNow() {
        // given
        String tokenExpiringNow = "token-expiring-now";
        User userWithTokenExpiringNow = User.builder()
                .verificationToken(tokenExpiringNow)
                .tokenExpirationTime(LocalDateTime.now().minusSeconds(1)) // expired 1 second ago
                .build();

        when(userRepository.findByVerificationToken(tokenExpiringNow))
                .thenReturn(Optional.of(userWithTokenExpiringNow));

        // when
        String result = handlerVerificationToken.validateVerificationToken(tokenExpiringNow);

        // then
        assertThat(result).isEqualTo("expired");
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldEnableUserWhenTokenIsValid() {
        // given
        String validToken = "valid-token-456";
        User disabledUser = User.builder()
                .verificationToken(validToken)
                .tokenExpirationTime(LocalDateTime.now().plusMinutes(30))
                .enabled(false)
                .username("johndoe")
                .email("john@example.com")
                .build();

        when(userRepository.findByVerificationToken(validToken))
                .thenReturn(Optional.of(disabledUser));

        // when
        String result = handlerVerificationToken.validateVerificationToken(validToken);

        // then
        assertThat(result).isEqualTo("valid");
        
        verify(userRepository).save(disabledUser);
        assertThat(disabledUser.isEnabled()).isTrue();
        assertThat(disabledUser.getUsername()).isEqualTo("johndoe");
        assertThat(disabledUser.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void shouldHandleNullTokenInput() {
        // given
        when(userRepository.findByVerificationToken(null))
                .thenReturn(Optional.empty());

        // when
        String result = handlerVerificationToken.validateVerificationToken(null);

        // then
        assertThat(result).isEqualTo("invalid");
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldHandleEmptyTokenInput() {
        // given
        String emptyToken = "";
        when(userRepository.findByVerificationToken(emptyToken))
                .thenReturn(Optional.empty());

        // when
        String result = handlerVerificationToken.validateVerificationToken(emptyToken);

        // then
        assertThat(result).isEqualTo("invalid");
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldValidateTokenOnExactExpirationBoundary() {
        // given
        String tokenOnBoundary = "boundary-token";
        LocalDateTime exactExpirationTime = LocalDateTime.now().plusNanos(500_000_000); // 0.5 seconds
        
        User userWithTokenOnBoundary = User.builder()
                .verificationToken(tokenOnBoundary)
                .tokenExpirationTime(exactExpirationTime)
                .build();

        when(userRepository.findByVerificationToken(tokenOnBoundary))
                .thenReturn(Optional.of(userWithTokenOnBoundary));

        // when
        String result = handlerVerificationToken.validateVerificationToken(tokenOnBoundary);

        // then
        // Since we can't predict exact timing, the result should be either "valid" or "expired"
        assertThat(result).isIn("valid", "expired");
    }

    @Test
    void shouldNotModifyOtherUserFieldsWhenEnabling() {
        // given
        String validToken = "preserve-fields-token";
        User originalUser = User.builder()
                .verificationToken(validToken)
                .tokenExpirationTime(LocalDateTime.now().plusHours(2))
                .firstName("Original")
                .lastName("User")
                .email("original@example.com")
                .enabled(false)
                .build();

        when(userRepository.findByVerificationToken(validToken))
                .thenReturn(Optional.of(originalUser));

        // when
        String result = handlerVerificationToken.validateVerificationToken(validToken);

        // then
        assertThat(result).isEqualTo("valid");
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.isEnabled()).isTrue();
        assertThat(savedUser.getFirstName()).isEqualTo("Original");
        assertThat(savedUser.getLastName()).isEqualTo("User");
        assertThat(savedUser.getEmail()).isEqualTo("original@example.com");
        assertThat(savedUser.getVerificationToken()).isEqualTo(validToken);
    }
}