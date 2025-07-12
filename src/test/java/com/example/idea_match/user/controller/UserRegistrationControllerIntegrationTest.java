package com.example.idea_match.user.controller;

import com.example.idea_match.user.command.AddUserCommand;
import com.example.idea_match.user.model.User;
import com.example.idea_match.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserRegistrationControllerIntegrationTest {

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
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    private AddUserCommand validCommand;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        // Database is cleaned by @Sql annotation
        
        validCommand = new AddUserCommand(
                "John",
                "Doe",
                "johndoe",
                "k.kolodziej2212@gmail.com",
                "+48123456789",
                "Warsaw",
                "Software developer",
                "Password123!"
        );

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        // given
        HttpEntity<AddUserCommand> request = new HttpEntity<>(validCommand, headers);
        String url = "http://localhost:" + port + "/api/registration";

        // when
        ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNull();

        // Verify user is saved in database
        Optional<User> savedUser = userRepository.findByUsername("johndoe");
        assertThat(savedUser).isPresent();

        User user = savedUser.get();
        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getEmail()).isEqualTo("k.kolodziej2212@gmail.com");
        assertThat(user.getPhoneNumber()).isEqualTo("+48123456789");
        assertThat(user.isEnabled()).isFalse();
        assertThat(user.getVerificationToken()).isNotNull();
        assertThat(user.getPassword()).isNotEqualTo("Password123!"); // Should be encoded
    }

    @Test
    void shouldReturnConflictWhenUserAlreadyExists() {
        // given - create existing user
        userRepository.save(User.builder()
                .firstName("Jane")
                .lastName("Smith")
                .username("johndoe") // same username
                .email("k.kolodziej2212@gmail.com")
                .phoneNumber("+48987654321")
                .password("encoded_password")
                .enabled(false)
                .role(com.example.idea_match.user.model.Role.USER)
                .verificationToken("test-token")
                .tokenExpirationTime(LocalDateTime.now().plusHours(24))
                .build());

        HttpEntity<AddUserCommand> request = new HttpEntity<>(validCommand, headers);
        String url = "http://localhost:" + port + "/api/registration";

        // when
        ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(userRepository.count()).isEqualTo(1); // No additional user created
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() {
        // given
        userRepository.save(User.builder()
                .firstName("Jane")
                .lastName("Smith")
                .username("janesmith")
                .email("k.kolodziej2212@gmail.com") // same email
                .phoneNumber("+48987654321")
                .password("encoded_password")
                .enabled(false)
                .role(com.example.idea_match.user.model.Role.USER)
                .verificationToken("test-token")
                .tokenExpirationTime(LocalDateTime.now().plusHours(24))
                .build());

        HttpEntity<AddUserCommand> request = new HttpEntity<>(validCommand, headers);
        String url = "http://localhost:" + port + "/api/registration";

        // when
        ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldReturnBadRequestForInvalidEmail() {
        // given
        AddUserCommand invalidCommand = new AddUserCommand(
                "John",
                "Doe",
                "johndoe",
                "invalid-email", // invalid email format
                "+48123456789",
                "Warsaw",
                "Software developer",
                "Password123!"
        );

        HttpEntity<AddUserCommand> request = new HttpEntity<>(invalidCommand, headers);
        String url = "http://localhost:" + port + "/api/registration";

        // when
        ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(userRepository.count()).isEqualTo(0);
    }

    @Test
    void shouldReturnBadRequestForEmptyFields() {
        // given
        AddUserCommand invalidCommand = new AddUserCommand(
                "", // empty first name
                "Doe",
                "johndoe",
                "k.kolodziej2212@gmail.com",
                "+48123456789",
                "Warsaw",
                "Software developer",
                "" // empty password
        );

        HttpEntity<AddUserCommand> request = new HttpEntity<>(invalidCommand, headers);
        String url = "http://localhost:" + port + "/api/registration";

        // when
        ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(userRepository.count()).isEqualTo(0);
    }

    @Test
    void shouldHandleMultipleValidRegistrations() {
        // given
        AddUserCommand secondCommand = new AddUserCommand(
                "Jane",
                "Smith",
                "janesmith",
                "idea.match.contact@gmail.com",
                "+48987654321",
                "Krakow",
                "Designer",
                "Password456!"
        );

        String url = "http://localhost:" + port + "/api/registration";

        // when - register first user
        HttpEntity<AddUserCommand> firstRequest = new HttpEntity<>(validCommand, headers);
        ResponseEntity<String> firstResponse = restTemplate.postForEntity(url, firstRequest, String.class);

        // when - register second user
        HttpEntity<AddUserCommand> secondRequest = new HttpEntity<>(secondCommand, headers);
        ResponseEntity<String> secondResponse = restTemplate.postForEntity(url, secondRequest, String.class);

        // then
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(userRepository.count()).isEqualTo(2);
        
        assertThat(userRepository.findByUsername("johndoe")).isPresent();
        assertThat(userRepository.findByUsername("janesmith")).isPresent();
    }

    @Test
    void shouldReturnUnsupportedMediaTypeForWrongContentType() {
        // given
        HttpHeaders xmlHeaders = new HttpHeaders();
        xmlHeaders.setContentType(MediaType.APPLICATION_XML);
        String url = "http://localhost:" + port + "/api/registration";

        // when & then - TestRestTemplate throws exception when content type is not supported
        try {
            HttpEntity<String> request = new HttpEntity<>("{\"test\":\"data\"}", xmlHeaders);
            ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        } catch (org.springframework.web.client.RestClientException e) {
            // Expected exception for unsupported content type
            assertThat(e.getMessage()).contains("No HttpMessageConverter");
        }
        
        assertThat(userRepository.count()).isEqualTo(0);
    }

    @Test
    void shouldValidatePhoneNumberFormat() {
        // given
        AddUserCommand validPhoneCommand = new AddUserCommand(
                "John",
                "Doe",
                "johndoe",
                "idea.match.contact@gmail.com",
                "+15551234567",
                "Warsaw",
                "Software developer",
                "Password123!"
        );

        HttpEntity<AddUserCommand> request = new HttpEntity<>(validPhoneCommand, headers);
        String url = "http://localhost:" + port + "/api/registration";

        // when
        ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        Optional<User> savedUser = userRepository.findByUsername("johndoe");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getPhoneNumber()).isEqualTo("+15551234567");
    }

    @Test
    void shouldEncodePasswordProperly() {
        // given
        HttpEntity<AddUserCommand> request = new HttpEntity<>(validCommand, headers);
        String url = "http://localhost:" + port + "/api/registration";

        // when
        ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        Optional<User> savedUser = userRepository.findByUsername("johndoe");
        assertThat(savedUser).isPresent();
        
        User user = savedUser.get();
        assertThat(user.getPassword()).isNotEqualTo("Password123!");
        assertThat(user.getPassword()).startsWith("{bcrypt}$2a$");
    }

    @Test
    void shouldGenerateVerificationTokenAndExpirationTime() {
        // given
        HttpEntity<AddUserCommand> request = new HttpEntity<>(validCommand, headers);
        String url = "http://localhost:" + port + "/api/registration";

        // when
        ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        Optional<User> savedUser = userRepository.findByUsername("johndoe");
        assertThat(savedUser).isPresent();
        
        User user = savedUser.get();
        assertThat(user.getVerificationToken()).isNotNull();
        assertThat(user.getVerificationToken()).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        assertThat(user.getTokenExpirationTime()).isNotNull();
        assertThat(user.getTokenExpirationTime()).isAfter(java.time.LocalDateTime.now().plusHours(23));
    }
}