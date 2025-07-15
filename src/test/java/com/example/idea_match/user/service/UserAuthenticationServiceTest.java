package com.example.idea_match.user.service;

import com.example.idea_match.user.config.CustomUserDetails;
import com.example.idea_match.user.dto.LoginRequest;
import com.example.idea_match.user.jwt.JwtTokenProvider;
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
    @DisplayName("Should login successfully with valid credentials")
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
    @DisplayName("Should create authentication token with correct parameters")
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

}