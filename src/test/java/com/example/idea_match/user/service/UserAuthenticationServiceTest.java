package com.example.idea_match.user.service;

import com.example.idea_match.shared.security.auth.CustomUserDetails;
import com.example.idea_match.user.command.LoginCommand;
import com.example.idea_match.user.command.ForgotPasswordCommand;
import com.example.idea_match.user.command.ResetPasswordCommand;
import com.example.idea_match.user.event.PasswordResetCompletedEvent;
import com.example.idea_match.user.event.PasswordResetRequestedEvent;
import com.example.idea_match.user.exceptions.InvalidTokenException;
import org.springframework.context.ApplicationEventPublisher;
import com.example.idea_match.shared.security.jwt.JwtTokenProvider;
import com.example.idea_match.user.model.Role;
import com.example.idea_match.user.model.User;
import com.example.idea_match.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserAuthenticationService Unit Tests")
class UserAuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private Authentication authentication;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private UserAuthenticationService userAuthenticationService;

    private LoginCommand loginCommand;
    private User testUser;
    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        loginCommand = new LoginCommand("johndoe", "password123");
        
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .email("john@example.com")
                .phoneNumber("+48123456789")
                .location("Warsaw")
                .aboutMe("Software developer")
                .password("encodedPassword")
                .enabled(true)
                .role(Role.USER)
                .verificationToken("test-token")
                .tokenExpirationTime(LocalDateTime.now().plusHours(24))
                .build();
        
        customUserDetails = new CustomUserDetails(testUser);
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfully() {
        // given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(jwtTokenProvider.createAccessToken(testUser)).thenReturn("jwt-token");

        // when
        String result = userAuthenticationService.login(loginCommand);

        // then
        assertThat(result).isEqualTo("jwt-token");
        
        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor = 
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(authCaptor.capture());
        
        UsernamePasswordAuthenticationToken capturedAuth = authCaptor.getValue();
        assertThat(capturedAuth.getPrincipal()).isEqualTo("johndoe");
        assertThat(capturedAuth.getCredentials()).isEqualTo("password123");
        
        verify(jwtTokenProvider).createAccessToken(testUser);
    }

    @Test
    @DisplayName("Should create authentication token with correct parameters")
    void shouldCreateAuthenticationTokenWithCorrectParameters() {
        // given
        LoginCommand customLoginCommand = new LoginCommand("testuser", "testpass");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(jwtTokenProvider.createAccessToken(any())).thenReturn("test-token");

        // when
        userAuthenticationService.login(customLoginCommand);

        // then
        ArgumentCaptor<UsernamePasswordAuthenticationToken> tokenCaptor = 
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(tokenCaptor.capture());
        
        UsernamePasswordAuthenticationToken capturedToken = tokenCaptor.getValue();
        assertThat(capturedToken.getPrincipal()).isEqualTo("testuser");
        assertThat(capturedToken.getCredentials()).isEqualTo("testpass");
        assertThat(capturedToken.getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("Should initiate password reset successfully")
    void shouldInitiatePasswordResetSuccessfully() {
        // given
        ForgotPasswordCommand command = new ForgotPasswordCommand("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(java.util.Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // when
        userAuthenticationService.initiatePasswordReset(command);

        // then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPasswordResetToken()).isNotNull();
        assertThat(savedUser.getPasswordResetTokenExpiry()).isNotNull();
        
        verify(applicationEventPublisher).publishEvent(any(PasswordResetRequestedEvent.class));
    }

    @Test
    @DisplayName("Should reset password successfully")
    void shouldResetPasswordSuccessfully() {
        // given
        String resetToken = "reset-token";
        String newPassword = "newPassword123";
        ResetPasswordCommand command = new ResetPasswordCommand(resetToken, newPassword);
        
        testUser.setPasswordResetToken(resetToken);
        testUser.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
        
        when(userRepository.findByPasswordResetToken(resetToken)).thenReturn(java.util.Optional.of(testUser));
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // when
        userAuthenticationService.resetPassword(command);

        // then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo("encodedNewPassword");
        assertThat(savedUser.getPasswordResetToken()).isNull();
        assertThat(savedUser.getPasswordResetTokenExpiry()).isNull();
        
        verify(applicationEventPublisher).publishEvent(any(PasswordResetCompletedEvent.class));
    }

    @Test
    @DisplayName("Should throw exception for expired reset token")
    void shouldThrowExceptionForExpiredResetToken() {
        // given
        String resetToken = "expired-token";
        String newPassword = "newPassword123";
        ResetPasswordCommand command = new ResetPasswordCommand(resetToken, newPassword);
        
        testUser.setPasswordResetToken(resetToken);
        testUser.setPasswordResetTokenExpiry(LocalDateTime.now().minusHours(1)); // expired
        
        when(userRepository.findByPasswordResetToken(resetToken)).thenReturn(java.util.Optional.of(testUser));

        // when & then
        assertThatThrownBy(() -> userAuthenticationService.resetPassword(command))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Reset token has expired");
        
        verify(userRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

}