package com.example.idea_match.user.service;

import com.example.idea_match.user.config.CustomUserDetails;
import com.example.idea_match.user.dto.LoginRequest;
import com.example.idea_match.user.jwt.JwtTokenProvider;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    @InjectMocks
    private UserAuthenticationService userAuthenticationService;

    private LoginRequest loginRequest;
    private User testUser;
    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("johndoe", "password123");
        
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
    void shouldLoginSuccessfully() {
        // given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(jwtTokenProvider.createAccessToken(testUser)).thenReturn("jwt-token");

        // when
        String result = userAuthenticationService.login(loginRequest);

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
    void shouldAuthenticateWithEmail() {
        // given
        LoginRequest emailLoginRequest = new LoginRequest("john@example.com", "password123");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(jwtTokenProvider.createAccessToken(testUser)).thenReturn("jwt-token");

        // when
        String result = userAuthenticationService.login(emailLoginRequest);

        // then
        assertThat(result).isEqualTo("jwt-token");
        
        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor = 
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(authCaptor.capture());
        
        UsernamePasswordAuthenticationToken capturedAuth = authCaptor.getValue();
        assertThat(capturedAuth.getPrincipal()).isEqualTo("john@example.com");
        assertThat(capturedAuth.getCredentials()).isEqualTo("password123");
    }

    @Test
    void shouldThrowBadCredentialsExceptionForInvalidCredentials() {
        // given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // when & then
        assertThatThrownBy(() -> userAuthenticationService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider, never()).createAccessToken(any());
    }

    @Test
    void shouldThrowDisabledExceptionForDisabledUser() {
        // given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DisabledException("User account is disabled"));

        // when & then
        assertThatThrownBy(() -> userAuthenticationService.login(loginRequest))
                .isInstanceOf(DisabledException.class)
                .hasMessage("User account is disabled");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider, never()).createAccessToken(any());
    }

    @Test
    void shouldCreateTokenWithCorrectUser() {
        // given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(jwtTokenProvider.createAccessToken(testUser)).thenReturn("expected-jwt-token");

        // when
        String result = userAuthenticationService.login(loginRequest);

        // then
        assertThat(result).isEqualTo("expected-jwt-token");
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(jwtTokenProvider).createAccessToken(userCaptor.capture());
        
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getId()).isEqualTo(1L);
        assertThat(capturedUser.getUsername()).isEqualTo("johndoe");
        assertThat(capturedUser.getEmail()).isEqualTo("john@example.com");
        assertThat(capturedUser.getRole()).isEqualTo(Role.USER);
    }


    @Test
    void shouldHandleNullAuthenticationResult() {
        // given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);

        // when & then
        assertThatThrownBy(() -> userAuthenticationService.login(loginRequest))
                .isInstanceOf(NullPointerException.class);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider, never()).createAccessToken(any());
    }

    @Test
    void shouldCreateAuthenticationTokenWithCorrectParameters() {
        // given
        LoginRequest customLoginRequest = new LoginRequest("testuser", "testpass");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(jwtTokenProvider.createAccessToken(any())).thenReturn("test-token");

        // when
        userAuthenticationService.login(customLoginRequest);

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
    void shouldReturnTokenFromJwtTokenProvider() {
        // given
        String expectedToken = "very-long-jwt-token-with-specific-content";
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(jwtTokenProvider.createAccessToken(testUser)).thenReturn(expectedToken);

        // when
        String actualToken = userAuthenticationService.login(loginRequest);

        // then
        assertThat(actualToken).isEqualTo(expectedToken);
        verify(jwtTokenProvider).createAccessToken(testUser);
    }
}