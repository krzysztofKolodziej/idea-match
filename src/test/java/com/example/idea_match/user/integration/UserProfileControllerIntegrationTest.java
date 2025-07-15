package com.example.idea_match.user.integration;

import com.example.idea_match.user.command.UpdateUserProfileCommand;
import com.example.idea_match.user.dto.AuthResponse;
import com.example.idea_match.user.dto.LoginRequest;
import com.example.idea_match.user.dto.UserResponse;
import com.example.idea_match.user.model.Role;
import com.example.idea_match.user.model.User;
import com.example.idea_match.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserProfileControllerIntegrationTest {

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
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .email("john@example.com")
                .phoneNumber("+48123456789")
                .location("Warsaw")
                .aboutMe("Software developer")
                .password(passwordEncoder.encode("password123"))
                .enabled(true)
                .role(Role.USER)
                .verificationToken("test-token")
                .tokenExpirationTime(LocalDateTime.now().plusHours(24))
                .build();
        
        userRepository.save(testUser);
    }

    @Test
    void shouldGetUserProfileSuccessfully() {
        // given
        String jwtToken = authenticateAndGetJwtToken("johndoe", "password123");

        // when
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<UserResponse> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/account/profile",
                HttpMethod.GET,
                request,
                UserResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        UserResponse userResponse = response.getBody();
        assertThat(userResponse.userId()).isEqualTo(testUser.getId());
        assertThat(userResponse.firstName()).isEqualTo("John");
        assertThat(userResponse.lastName()).isEqualTo("Doe");
        assertThat(userResponse.username()).isEqualTo("johndoe");
        assertThat(userResponse.email()).isEqualTo("john@example.com");
        assertThat(userResponse.phoneNumber()).isEqualTo("+48123456789");
        assertThat(userResponse.location()).isEqualTo("Warsaw");
        assertThat(userResponse.aboutMe()).isEqualTo("Software developer");
        assertThat(userResponse.profilePictureUrl()).isNull();
    }

    @Test
    void shouldGetUserProfileWithEmailAuthentication() {
        // given
        String jwtToken = authenticateAndGetJwtToken("john@example.com", "password123");

        // when
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<UserResponse> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/account/profile",
                HttpMethod.GET,
                request,
                UserResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        UserResponse userResponse = response.getBody();
        assertThat(userResponse.userId()).isEqualTo(testUser.getId());
        assertThat(userResponse.username()).isEqualTo("johndoe");
        assertThat(userResponse.email()).isEqualTo("john@example.com");
    }

    @Test
    void shouldReturnUnauthorizedWhenInvalidToken() {
        // given
        String invalidToken = "invalid.jwt.token";

        // when
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(invalidToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/account/profile",
                HttpMethod.GET,
                request,
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldDeleteUserAccountSuccessfully() {
        // given
        String jwtToken = authenticateAndGetJwtToken("johndoe", "password123");

        // when
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/account/profile",
                HttpMethod.DELETE,
                request,
                Void.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(userRepository.findByUsername("johndoe")).isEmpty();
    }

    @Test
    void shouldDeleteUserAccountWithEmailAuthentication() {
        // given
        String jwtToken = authenticateAndGetJwtToken("john@example.com", "password123");

        // when
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/account/profile",
                HttpMethod.DELETE,
                request,
                Void.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(userRepository.findByEmail("john@example.com")).isEmpty();
    }

    @Test
    void shouldUpdateUserProfileSuccessfully() {
        // given
        String jwtToken = authenticateAndGetJwtToken("johndoe", "password123");
        UpdateUserProfileCommand updateCommand = new UpdateUserProfileCommand(
                "Jane",
                "Smith",
                "+48987654321",
                "Krakow",
                "Senior developer"
        );

        // when
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UpdateUserProfileCommand> request = new HttpEntity<>(updateCommand, headers);

        ResponseEntity<UserResponse> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/account/profile",
                HttpMethod.PUT,
                request,
                UserResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        UserResponse updatedUser = response.getBody();
        assertThat(updatedUser.firstName()).isEqualTo("Jane");
        assertThat(updatedUser.lastName()).isEqualTo("Smith");
        assertThat(updatedUser.phoneNumber()).isEqualTo("+48987654321");
        assertThat(updatedUser.location()).isEqualTo("Krakow");
        assertThat(updatedUser.aboutMe()).isEqualTo("Senior developer");
        assertThat(updatedUser.username()).isEqualTo("johndoe");
        assertThat(updatedUser.email()).isEqualTo("john@example.com");
    }

    @Test
    void shouldUpdateUserProfileWithEmailAuthentication() {
        // given
        String jwtToken = authenticateAndGetJwtToken("john@example.com", "password123");
        UpdateUserProfileCommand updateCommand = new UpdateUserProfileCommand(
                "Jane",
                "Smith",
                "+48987654321",
                "Krakow",
                "Senior developer"
        );

        // when
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UpdateUserProfileCommand> request = new HttpEntity<>(updateCommand, headers);

        ResponseEntity<UserResponse> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/account/profile",
                HttpMethod.PUT,
                request,
                UserResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        UserResponse updatedUser = response.getBody();
        assertThat(updatedUser.firstName()).isEqualTo("Jane");
        assertThat(updatedUser.lastName()).isEqualTo("Smith");
        assertThat(updatedUser.phoneNumber()).isEqualTo("+48987654321");
        assertThat(updatedUser.location()).isEqualTo("Krakow");
        assertThat(updatedUser.aboutMe()).isEqualTo("Senior developer");
    }

    @Test
    void shouldReturnBadRequestWhenUpdateProfileWithInvalidData() {
        // given
        String jwtToken = authenticateAndGetJwtToken("johndoe", "password123");
        UpdateUserProfileCommand updateCommand = new UpdateUserProfileCommand(
                "",  // Empty firstName should fail validation
                "Smith",
                "invalid-phone-number",
                "Krakow",
                "Senior developer"
        );

        // when
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UpdateUserProfileCommand> request = new HttpEntity<>(updateCommand, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/account/profile",
                HttpMethod.PUT,
                request,
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnConflictWhenUpdateProfileWithExistingPhoneNumber() {
        // given - create another user with different phone number
        User anotherUser = User.builder()
                .firstName("Jane")
                .lastName("Smith")
                .username("janesmith")
                .email("jane@example.com")
                .phoneNumber("+48111222333")
                .password(passwordEncoder.encode("password123"))
                .enabled(true)
                .role(Role.USER)
                .build();
        userRepository.save(anotherUser);

        String jwtToken = authenticateAndGetJwtToken("johndoe", "password123");
        UpdateUserProfileCommand updateCommand = new UpdateUserProfileCommand(
                "John",
                "Updated",
                "+48111222333", // Using Jane's phone number
                "Warsaw",
                "Developer"
        );

        // when
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UpdateUserProfileCommand> request = new HttpEntity<>(updateCommand, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/account/profile",
                HttpMethod.PUT,
                request,
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("Phone number already exists");
    }

    private String authenticateAndGetJwtToken(String usernameOrEmail, String password) {
        LoginRequest loginRequest = new LoginRequest(usernameOrEmail, password);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/login", request, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return response.getBody().getToken();
    }
}