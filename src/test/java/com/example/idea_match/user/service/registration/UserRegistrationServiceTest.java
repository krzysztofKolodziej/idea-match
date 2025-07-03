package com.example.idea_match.user.service.registration;

import com.example.idea_match.user.command.AddUserCommand;
import com.example.idea_match.user.event.OnRegistrationCompleteEvent;
import com.example.idea_match.user.exceptions.UserAlreadyExistsException;
import com.example.idea_match.user.model.Role;
import com.example.idea_match.user.model.User;
import com.example.idea_match.user.repository.UserRepository;
import com.example.idea_match.user.service.HandlerVerificationToken;
import com.example.idea_match.user.service.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private HandlerVerificationToken handlerVerificationToken;

    @InjectMocks
    private UserRegistrationService userRegistrationService;

    private AddUserCommand validCommand;
    private User mappedUser;

    @BeforeEach
    void setUp() {
        validCommand = new AddUserCommand(
                "John",
                "Doe",
                "johndoe",
                "john@example.com",
                "+48123456789",
                "Warsaw",
                "Software developer",
                "password123"
        );

        mappedUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .email("john@example.com")
                .phoneNumber("+48123456789")
                .location("Warsaw")
                .aboutMe("Software developer")
                .password("password123")
                .enabled(false)
                .role(Role.USER)
                .build();
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        // given
        when(userRepository.existsByUsernameOrEmailOrPhoneNumber(
                validCommand.username(),
                validCommand.email(),
                validCommand.phoneNumber()
        )).thenReturn(false);
        when(userMapper.dtoToEntity(validCommand)).thenReturn(mappedUser);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(handlerVerificationToken.createVerificationToken(any(User.class), any(LocalDateTime.class)))
                .thenReturn(mappedUser);

        // when
        userRegistrationService.userRegistration(validCommand);

        // then
        verify(handlerVerificationToken).createVerificationToken(any(User.class), any(LocalDateTime.class));
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");

        verify(eventPublisher).publishEvent(any(OnRegistrationCompleteEvent.class));
    }

    @Test
    void shouldThrowExceptionWhenUserAlreadyExists() {
        // given
        when(userRepository.existsByUsernameOrEmailOrPhoneNumber(
                validCommand.username(),
                validCommand.email(),
                validCommand.phoneNumber()
        )).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userRegistrationService.userRegistration(validCommand))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Provided user, email or phone number exist");

        verify(userMapper, never()).dtoToEntity(any());
        verify(userRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldEncodePasswordCorrectly() {
        // given
        when(userRepository.existsByUsernameOrEmailOrPhoneNumber(anyString(), anyString(), anyString()))
                .thenReturn(false);
        when(userMapper.dtoToEntity(validCommand)).thenReturn(mappedUser);
        when(passwordEncoder.encode("password123")).thenReturn("super-secure-hash");
        when(handlerVerificationToken.createVerificationToken(any(User.class), any(LocalDateTime.class)))
                .thenReturn(mappedUser);

        // when
        userRegistrationService.userRegistration(validCommand);

        // then
        verify(passwordEncoder).encode("password123");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("super-secure-hash");
    }

    @Test
    void shouldGenerateVerificationToken() {
        // given
        when(userRepository.existsByUsernameOrEmailOrPhoneNumber(anyString(), anyString(), anyString()))
                .thenReturn(false);
        when(userMapper.dtoToEntity(validCommand)).thenReturn(mappedUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        User userWithToken = User.builder()
                .verificationToken("test-token-uuid")
                .build();
        when(handlerVerificationToken.createVerificationToken(any(User.class), any(LocalDateTime.class)))
                .thenReturn(userWithToken);

        // when
        userRegistrationService.userRegistration(validCommand);

        // then
        verify(handlerVerificationToken).createVerificationToken(any(User.class), any(LocalDateTime.class));
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getVerificationToken()).isNotNull();
        assertThat(savedUser.getVerificationToken()).isEqualTo("test-token-uuid");
    }

    @Test
    void shouldPublishRegistrationEvent() {
        // given
        when(userRepository.existsByUsernameOrEmailOrPhoneNumber(anyString(), anyString(), anyString()))
                .thenReturn(false);
        when(userMapper.dtoToEntity(validCommand)).thenReturn(mappedUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(handlerVerificationToken.createVerificationToken(any(User.class), any(LocalDateTime.class)))
                .thenReturn(mappedUser);

        // when
        userRegistrationService.userRegistration(validCommand);

        // then
        ArgumentCaptor<OnRegistrationCompleteEvent> eventCaptor = ArgumentCaptor.forClass(OnRegistrationCompleteEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        
        OnRegistrationCompleteEvent event = eventCaptor.getValue();
        assertThat(event.getUser()).isNotNull();
        assertThat(event.getUser().getUsername()).isEqualTo("johndoe");
        assertThat(event.getSource()).isEqualTo(userRegistrationService);
    }

    @Test
    void shouldSetTokenExpirationTimeTo24Hours() {
        // given
        LocalDateTime beforeTest = LocalDateTime.now();
        when(userRepository.existsByUsernameOrEmailOrPhoneNumber(anyString(), anyString(), anyString()))
                .thenReturn(false);
        when(userMapper.dtoToEntity(validCommand)).thenReturn(mappedUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        User userWithToken = User.builder()
                .tokenExpirationTime(LocalDateTime.now().plusHours(24))
                .build();
        when(handlerVerificationToken.createVerificationToken(any(User.class), any(LocalDateTime.class)))
                .thenReturn(userWithToken);

        // when
        userRegistrationService.userRegistration(validCommand);

        // then
        verify(handlerVerificationToken).createVerificationToken(any(User.class), any(LocalDateTime.class));
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        LocalDateTime afterTest = LocalDateTime.now();
        
        assertThat(savedUser.getTokenExpirationTime())
                .isAfter(beforeTest.plusHours(23).plusMinutes(59))
                .isBefore(afterTest.plusHours(24).plusMinutes(1));
    }
}