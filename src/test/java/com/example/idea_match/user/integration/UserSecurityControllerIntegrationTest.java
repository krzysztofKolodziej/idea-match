package com.example.idea_match.user.integration;

import com.example.idea_match.config.TestConfig;
import com.example.idea_match.user.command.ChangePasswordCommand;
import com.example.idea_match.user.model.Role;
import com.example.idea_match.user.model.User;
import com.example.idea_match.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(TestConfig.class)
@DisplayName("UserSecurityController Integration Tests")
class UserSecurityControllerIntegrationTest {

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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    private String baseUrl;
    private User testUser;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/account";

        testUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .email("john@example.com")
                .phoneNumber("+48123456789")
                .location("Warsaw")
                .aboutMe("Software developer")
                .password(passwordEncoder.encode("currentPassword123"))
                .enabled(true)
                .role(Role.USER)
                .verificationToken("test-token")
                .tokenExpirationTime(LocalDateTime.now().plusHours(24))
                .build();

        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("Should change password successfully")
    void shouldChangePasswordSuccessfully() {
        // given
        String token = authenticateAndGetToken();
        ChangePasswordCommand request = new ChangePasswordCommand("currentPassword123", "NewSecurePass123!");
        HttpHeaders headers = createAuthHeaders(token);
        HttpEntity<ChangePasswordCommand> entity = new HttpEntity<>(request, headers);

        // when
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/change-password",
                HttpMethod.POST,
                entity,
                Void.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("NewSecurePass123!", updatedUser.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("currentPassword123", updatedUser.getPassword())).isFalse();
    }







    private String authenticateAndGetToken() {
        return authenticateUserAndGetToken("johndoe", "currentPassword123");
    }

    private String authenticateUserAndGetToken(String usernameOrEmail, String password) {
        String loginUrl = "http://localhost:" + port + "/api/login";
        String loginBody = String.format("{\"usernameOrEmail\":\"%s\",\"password\":\"%s\"}", usernameOrEmail, password);
        
        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> loginEntity = new HttpEntity<>(loginBody, loginHeaders);
        
        try {
            ResponseEntity<String> loginResponse = restTemplate.exchange(
                    loginUrl,
                    HttpMethod.POST,
                    loginEntity,
                    String.class
            );
            
            if (loginResponse.getStatusCode().is2xxSuccessful()) {
                String responseBody = loginResponse.getBody();
                // Parse JSON response to extract token
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    var authResponse = mapper.readTree(responseBody);
                    return authResponse.get("token").asText();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to parse login response: " + responseBody, e);
                }
            } else {
                throw new RuntimeException("Login failed with status: " + loginResponse.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
    }

    private HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }
}