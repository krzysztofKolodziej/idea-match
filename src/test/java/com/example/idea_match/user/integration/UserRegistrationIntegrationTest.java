package com.example.idea_match.user.integration;

import com.example.idea_match.user.command.AddUserCommand;
import com.example.idea_match.user.exceptions.UserAlreadyExistsException;
import com.example.idea_match.user.model.Role;
import com.example.idea_match.user.model.User;
import com.example.idea_match.user.repository.UserRepository;
import com.example.idea_match.user.service.registration.UserRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Testcontainers
@Transactional
@ActiveProfiles("test")
class UserRegistrationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
    }

    @Autowired
    private UserRegistrationService userRegistrationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private AddUserCommand validCommand;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        validCommand = new AddUserCommand(
                "John",
                "Doe",
                "johndoe",
                "k.kolodziej2212@gmail.com",
                "+48123456789",
                "Warsaw",
                "Software developer",
                "password123"
        );
    }

    @Test
    void shouldRegisterUserAndSaveToDatabase() {
        // when
        userRegistrationService.userRegistration(validCommand);

        // then
        Optional<User> savedUser = userRepository.findByUsername("johndoe");
        assertThat(savedUser).isPresent();

        User user = savedUser.get();
        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastname()).isEqualTo("Doe");
        assertThat(user.getUsername()).isEqualTo("johndoe");
        assertThat(user.getEmail()).isEqualTo("k.kolodziej2212@gmail.com");
        assertThat(user.getPhoneNumber()).isEqualTo("+48123456789");
        assertThat(user.getLocation()).isEqualTo("Warsaw");
        assertThat(user.getAboutMe()).isEqualTo("Software developer");
        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.isEnabled()).isFalse();
        
        // Password should be encoded
        assertThat(user.getPassword()).isNotEqualTo("password123");
        assertThat(user.getPassword()).startsWith("{bcrypt}$2a$");
        
        // Verification token should be generated
        assertThat(user.getVerificationToken()).isNotNull();
        assertThat(user.getVerificationToken()).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        
        // Token expiration should be set to 24 hours from now
        assertThat(user.getTokenExpirationTime()).isAfter(LocalDateTime.now().plusHours(23));
        assertThat(user.getTokenExpirationTime()).isBefore(LocalDateTime.now().plusHours(25));
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        // given
        User existingUser = User.builder()
                .firstName("Jane")
                .lastname("Smith")
                .username("johndoe") // same username
                .email("k.kolodziej2212@gmail.com")
                .phoneNumber("+48987654321")
                .password("encoded_password")
                .enabled(false)
                .role(Role.USER)
                .verificationToken("some-token")
                .tokenExpirationTime(LocalDateTime.now().plusHours(24))
                .build();
        
        userRepository.save(existingUser);

        // when & then
        assertThatThrownBy(() -> userRegistrationService.userRegistration(validCommand))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Provided user, email or phone number exist");

        // Verify no additional user was created
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // given
        User existingUser = User.builder()
                .firstName("Jane")
                .lastname("Smith")
                .username("janesmith")
                .email("k.kolodziej2212@gmail.com") // same email
                .phoneNumber("+48987654321")
                .password("encoded_password")
                .enabled(false)
                .role(Role.USER)
                .verificationToken("some-token")
                .tokenExpirationTime(LocalDateTime.now().plusHours(24))
                .build();
        
        userRepository.save(existingUser);

        // when & then
        assertThatThrownBy(() -> userRegistrationService.userRegistration(validCommand))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Provided user, email or phone number exist");

        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldThrowExceptionWhenPhoneNumberAlreadyExists() {
        // given
        User existingUser = User.builder()
                .firstName("Jane")
                .lastname("Smith")
                .username("janesmith")
                .email("k.kolodziej2212@gmail.com")
                .phoneNumber("+48123456789") // same phone number
                .password("encoded_password")
                .enabled(false)
                .role(Role.USER)
                .verificationToken("some-token")
                .tokenExpirationTime(LocalDateTime.now().plusHours(24))
                .build();
        
        userRepository.save(existingUser);

        // when & then
        assertThatThrownBy(() -> userRegistrationService.userRegistration(validCommand))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Provided user, email or phone number exist");

        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldHandleMultipleUsersRegistration() {
        // given
        AddUserCommand secondCommand = new AddUserCommand(
                "Jane",
                "Smith",
                "janesmith",
                "idea.match.contact@gmail.com",
                "+48987654321",
                "Krakow",
                "Designer",
                "password456"
        );

        // when
        userRegistrationService.userRegistration(validCommand);
        userRegistrationService.userRegistration(secondCommand);

        // then
        assertThat(userRepository.count()).isEqualTo(2);

        Optional<User> firstUser = userRepository.findByUsername("johndoe");
        Optional<User> secondUser = userRepository.findByUsername("janesmith");

        assertThat(firstUser).isPresent();
        assertThat(secondUser).isPresent();

        assertThat(firstUser.get().getEmail()).isEqualTo("k.kolodziej2212@gmail.com");
        assertThat(secondUser.get().getEmail()).isEqualTo("idea.match.contact@gmail.com");
    }

    @Test
    void shouldGenerateUniqueVerificationTokensForDifferentUsers() {
        // given
        AddUserCommand secondCommand = new AddUserCommand(
                "Jane",
                "Smith",
                "janesmith",
                "idea.match.contact@gmail.com",
                "+48987654321",
                "Krakow",
                "Designer",
                "password456"
        );

        // when
        userRegistrationService.userRegistration(validCommand);
        userRegistrationService.userRegistration(secondCommand);

        // then
        Optional<User> firstUser = userRepository.findByUsername("johndoe");
        Optional<User> secondUser = userRepository.findByUsername("janesmith");

        assertThat(firstUser).isPresent();
        assertThat(secondUser).isPresent();

        String firstToken = firstUser.get().getVerificationToken();
        String secondToken = secondUser.get().getVerificationToken();

        assertThat(firstToken).isNotEqualTo(secondToken);
        assertThat(firstToken).isNotNull();
        assertThat(secondToken).isNotNull();
    }
}