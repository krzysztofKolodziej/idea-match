package com.example.idea_match.user.service;

import com.example.idea_match.user.command.ChangePasswordCommand;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserSecurityService Unit Tests")
class UserSecurityServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserSecurityService userSecurityService;

    private User testUser;
    private ChangePasswordCommand changePasswordCommand;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .email("john@example.com")
                .phoneNumber("+48123456789")
                .location("Warsaw")
                .aboutMe("Software developer")
                .password("encodedOldPassword")
                .enabled(true)
                .role(Role.USER)
                .verificationToken("test-token")
                .tokenExpirationTime(LocalDateTime.now().plusHours(24))
                .build();

        changePasswordCommand = new ChangePasswordCommand("oldPassword123", "newPassword456");

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Should change password successfully with username")
    void shouldChangePasswordSuccessfullyWithUsername() {
        // given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("johndoe");
        when(userRepository.findByUsernameOrEmail(eq("johndoe"), eq(null)))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword123", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword456")).thenReturn("encodedNewPassword");

        // when
        userSecurityService.changePassword(changePasswordCommand);

        // then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo("encodedNewPassword");
        assertThat(savedUser.getId()).isEqualTo(1L);
        assertThat(savedUser.getUsername()).isEqualTo("johndoe");
        
        verify(passwordEncoder).matches("oldPassword123", "encodedOldPassword");
        verify(passwordEncoder).encode("newPassword456");
    }








}