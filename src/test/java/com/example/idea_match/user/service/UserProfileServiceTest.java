package com.example.idea_match.user.service;

import com.example.idea_match.user.command.UpdateUserProfileCommand;
import com.example.idea_match.user.dto.UserResponse;
import com.example.idea_match.user.exceptions.PhoneNumberAlreadyExistsException;
import com.example.idea_match.user.exceptions.UsernameOrEmailNotFoundException;
import com.example.idea_match.user.model.Role;
import com.example.idea_match.user.model.User;
import com.example.idea_match.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserProfileService userProfileService;

    private User testUser;
    private UserResponse testUserResponse;

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
                .password("encodedPassword")
                .enabled(true)
                .role(Role.USER)
                .verificationToken("test-token")
                .tokenExpirationTime(LocalDateTime.now().plusHours(24))
                .build();

        testUserResponse = new UserResponse(
                1L,
                "John",
                "Doe",
                "johndoe",
                "john@example.com",
                "+48123456789",
                null,
                "Warsaw",
                "Software developer"
        );

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void shouldGetUserSuccessfullyWithUsername() {
        // given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("johndoe");
        when(userRepository.findByUsernameOrEmail(eq("johndoe"), eq(null)))
                .thenReturn(Optional.of(testUser));
        when(userMapper.entityToDto(testUser)).thenReturn(testUserResponse);

        // when
        UserResponse result = userProfileService.getUser();

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.firstName()).isEqualTo("John");
        assertThat(result.lastName()).isEqualTo("Doe");
        assertThat(result.username()).isEqualTo("johndoe");
        assertThat(result.email()).isEqualTo("john@example.com");
        assertThat(result.phoneNumber()).isEqualTo("+48123456789");
        assertThat(result.location()).isEqualTo("Warsaw");
        assertThat(result.aboutMe()).isEqualTo("Software developer");

        verify(userRepository).findByUsernameOrEmail("johndoe", null);
        verify(userMapper).entityToDto(testUser);
    }

    @Test
    void shouldGetUserSuccessfullyWithEmail() {
        // given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("john@example.com");
        when(userRepository.findByUsernameOrEmail(eq(null), eq("john@example.com")))
                .thenReturn(Optional.of(testUser));
        when(userMapper.entityToDto(testUser)).thenReturn(testUserResponse);

        // when
        UserResponse result = userProfileService.getUser();

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("john@example.com");

        verify(userRepository).findByUsernameOrEmail(null, "john@example.com");
        verify(userMapper).entityToDto(testUser);
    }

    @Test
    void shouldThrowUsernameOrEmailNotFoundWhenUserNotExists() {
        // given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("nonexistent");
        when(userRepository.findByUsernameOrEmail(eq("nonexistent"), eq(null)))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userProfileService.getUser())
                .isInstanceOf(UsernameOrEmailNotFoundException.class)
                .hasMessage("User not found: nonexistent");

        verify(userRepository).findByUsernameOrEmail("nonexistent", null);
        verify(userMapper, never()).entityToDto(any());
    }

    @Test
    void shouldDeleteCurrentUserSuccessfully() {
        // given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("johndoe");
        when(userRepository.findByUsernameOrEmail(eq("johndoe"), eq(null)))
                .thenReturn(Optional.of(testUser));

        // when
        userProfileService.deleteCurrentUser();

        // then
        verify(userRepository).findByUsernameOrEmail("johndoe", null);
        verify(userRepository).delete(testUser);
    }

    @Test
    void shouldDeleteCurrentUserWithEmailSuccessfully() {
        // given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("john@example.com");
        when(userRepository.findByUsernameOrEmail(eq(null), eq("john@example.com")))
                .thenReturn(Optional.of(testUser));

        // when
        userProfileService.deleteCurrentUser();

        // then
        verify(userRepository).findByUsernameOrEmail(null, "john@example.com");
        verify(userRepository).delete(testUser);
    }

    @Test
    void shouldThrowUsernameOrEmailNotFoundWhenDeletingNonExistentUser() {
        // given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("nonexistent");
        when(userRepository.findByUsernameOrEmail(eq("nonexistent"), eq(null)))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userProfileService.deleteCurrentUser())
                .isInstanceOf(UsernameOrEmailNotFoundException.class)
                .hasMessage("User not found: nonexistent");

        verify(userRepository).findByUsernameOrEmail("nonexistent", null);
        verify(userRepository, never()).delete(any());
    }

    @Test
    void shouldUpdateUserProfileSuccessfully() {
        // given
        UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                "Jane",
                "Smith",
                "+48987654321",
                "Krakow",
                "Senior developer"
        );
        
        User updatedUser = User.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Smith")
                .username("johndoe")
                .email("john@example.com")
                .phoneNumber("+48987654321")
                .location("Krakow")
                .aboutMe("Senior developer")
                .password("encodedPassword")
                .enabled(true)
                .role(Role.USER)
                .build();
        
        UserResponse updatedUserResponse = new UserResponse(
                1L,
                "Jane",
                "Smith",
                "johndoe",
                "john@example.com",
                "+48987654321",
                null,
                "Krakow",
                "Senior developer"
        );

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("johndoe");
        when(userRepository.findByUsernameOrEmail(eq("johndoe"), eq(null)))
                .thenReturn(Optional.of(testUser));
        when(userRepository.existsByPhoneNumber("+48987654321")).thenReturn(false);
        when(userRepository.save(testUser)).thenReturn(updatedUser);
        when(userMapper.entityToDto(updatedUser)).thenReturn(updatedUserResponse);

        // when
        UserResponse result = userProfileService.updateUserProfile(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.firstName()).isEqualTo("Jane");
        assertThat(result.lastName()).isEqualTo("Smith");
        assertThat(result.phoneNumber()).isEqualTo("+48987654321");
        assertThat(result.location()).isEqualTo("Krakow");
        assertThat(result.aboutMe()).isEqualTo("Senior developer");

        verify(userRepository).findByUsernameOrEmail("johndoe", null);
        verify(userRepository).existsByPhoneNumber("+48987654321");
        verify(userMapper).updateUserFromCommand(command, testUser);
        verify(userRepository).save(testUser);
        verify(userMapper).entityToDto(updatedUser);
    }

    @Test
    void shouldUpdateUserProfileWithEmailSuccessfully() {
        // given
        UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                "Jane",
                "Smith",
                "+48987654321",
                "Krakow",
                "Senior developer"
        );

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("john@example.com");
        when(userRepository.findByUsernameOrEmail(eq(null), eq("john@example.com")))
                .thenReturn(Optional.of(testUser));
        when(userRepository.existsByPhoneNumber("+48987654321")).thenReturn(false);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.entityToDto(testUser)).thenReturn(testUserResponse);

        // when
        UserResponse result = userProfileService.updateUserProfile(command);

        // then
        assertThat(result).isNotNull();
        verify(userRepository).findByUsernameOrEmail(null, "john@example.com");
        verify(userRepository).existsByPhoneNumber("+48987654321");
        verify(userMapper).updateUserFromCommand(command, testUser);
        verify(userRepository).save(testUser);
        verify(userMapper).entityToDto(testUser);
    }

    @Test
    void shouldThrowUsernameOrEmailNotFoundWhenUpdatingNonExistentUser() {
        // given
        UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                "Jane",
                "Smith",
                "+48987654321",
                "Krakow",
                "Senior developer"
        );

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("nonexistent");
        when(userRepository.findByUsernameOrEmail(eq("nonexistent"), eq(null)))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userProfileService.updateUserProfile(command))
                .isInstanceOf(UsernameOrEmailNotFoundException.class)
                .hasMessage("User not found: nonexistent");

        verify(userRepository).findByUsernameOrEmail("nonexistent", null);
        verify(userMapper, never()).updateUserFromCommand(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowPhoneNumberAlreadyExistsWhenUpdatingWithExistingPhoneNumber() {
        // given
        UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                "Jane",
                "Smith",
                "+48111222333", // Different phone number
                "Krakow",
                "Senior developer"
        );

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("johndoe");
        when(userRepository.findByUsernameOrEmail(eq("johndoe"), eq(null)))
                .thenReturn(Optional.of(testUser));
        when(userRepository.existsByPhoneNumber("+48111222333")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userProfileService.updateUserProfile(command))
                .isInstanceOf(PhoneNumberAlreadyExistsException.class)
                .hasMessage("Phone number already exists: +48111222333");

        verify(userRepository).findByUsernameOrEmail("johndoe", null);
        verify(userRepository).existsByPhoneNumber("+48111222333");
        verify(userMapper, never()).updateUserFromCommand(any(), any());
        verify(userRepository, never()).save(any());
    }

}