package com.example.idea_match.user.service;

import com.example.idea_match.user.command.ChangePasswordCommand;
import com.example.idea_match.user.exceptions.IncorrectUserPassword;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    @Test
    void shouldChangePasswordSuccessfullyWithEmail() {
        // given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("john@example.com");
        when(userRepository.findByUsernameOrEmail(eq(null), eq("john@example.com")))
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
        
        verify(userRepository).findByUsernameOrEmail(null, "john@example.com");
    }

    @Test
    void shouldThrowUsernameNotFoundExceptionWhenUserNotExists() {
        // given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("nonexistent");
        when(userRepository.findByUsernameOrEmail(eq("nonexistent"), eq(null)))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userSecurityService.changePassword(changePasswordCommand))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found: nonexistent");

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void shouldThrowIncorrectUserPasswordWhenOldPasswordIsWrong() {
        // given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("johndoe");
        when(userRepository.findByUsernameOrEmail(eq("johndoe"), eq(null)))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword123", "encodedOldPassword")).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> userSecurityService.changePassword(changePasswordCommand))
                .isInstanceOf(IncorrectUserPassword.class)
                .hasMessage("Old password is incorrect");

        verify(userRepository, never()).save(any());
        verify(passwordEncoder).matches("oldPassword123", "encodedOldPassword");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void shouldCorrectlyIdentifyEmailVsUsername() {
        // given - test with email
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByUsernameOrEmail(eq(null), eq("test@example.com")))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        // when
        userSecurityService.changePassword(changePasswordCommand);

        // then
        verify(userRepository).findByUsernameOrEmail(null, "test@example.com");
        verify(userRepository, never()).findByUsernameOrEmail(eq("test@example.com"), eq(null));
    }

    @Test
    void shouldCorrectlyIdentifyUsernameVsEmail() {
        // given - test with username (no @ symbol)
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsernameOrEmail(eq("testuser"), eq(null)))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        // when
        userSecurityService.changePassword(changePasswordCommand);

        // then
        verify(userRepository).findByUsernameOrEmail("testuser", null);
        verify(userRepository, never()).findByUsernameOrEmail(eq(null), eq("testuser"));
    }

    @Test
    void shouldEncodeNewPasswordCorrectly() {
        // given
        String expectedEncodedPassword = "super-secure-encoded-password";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("johndoe");
        when(userRepository.findByUsernameOrEmail(any(), any())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(passwordEncoder.encode("newPassword456")).thenReturn(expectedEncodedPassword);

        // when
        userSecurityService.changePassword(changePasswordCommand);

        // then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo(expectedEncodedPassword);
        verify(passwordEncoder).encode("newPassword456");
    }

    @Test
    void shouldNotModifyOtherUserFields() {
        // given
        String originalPassword = testUser.getPassword(); // Store original password before modification
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("johndoe");
        when(userRepository.findByUsernameOrEmail(any(), any())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("newEncodedPassword");

        // when
        userSecurityService.changePassword(changePasswordCommand);

        // then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getId()).isEqualTo(testUser.getId());
        assertThat(savedUser.getUsername()).isEqualTo(testUser.getUsername());
        assertThat(savedUser.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(savedUser.getFirstName()).isEqualTo(testUser.getFirstName());
        assertThat(savedUser.getLastName()).isEqualTo(testUser.getLastName());
        assertThat(savedUser.getRole()).isEqualTo(testUser.getRole());
        assertThat(savedUser.isEnabled()).isEqualTo(testUser.isEnabled());
        assertThat(savedUser.getPassword()).isNotEqualTo(originalPassword);
        assertThat(savedUser.getPassword()).isEqualTo("newEncodedPassword");
    }

    @Test
    void shouldCallRepositoryMethodsInCorrectOrder() {
        // given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("johndoe");
        when(userRepository.findByUsernameOrEmail(any(), any())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        // when
        userSecurityService.changePassword(changePasswordCommand);

        // then
        var inOrder = inOrder(userRepository, passwordEncoder);
        inOrder.verify(userRepository).findByUsernameOrEmail(any(), any());
        inOrder.verify(passwordEncoder).matches(any(), any());
        inOrder.verify(passwordEncoder).encode(any());
        inOrder.verify(userRepository).save(any());
    }
}