package com.example.idea_match.user.service;

import com.example.idea_match.user.dto.UserResponse;
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

}