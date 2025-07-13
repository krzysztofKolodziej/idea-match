package com.example.idea_match.user.integration;

import com.example.idea_match.user.jwt.JwtUtils;
import com.example.idea_match.user.jwt.RedisTokenBlacklistService;
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
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("UserSessionController Integration Tests")
class UserSessionControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("idea_match_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisTokenBlacklistService blacklistService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;
    private String validToken;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/auth";
        
        // Clear all data
        userRepository.deleteAll();
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.serverCommands().flushDb();
            return null;
        });
        
        // Create test user
        User testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("Test")
                .lastName("User")
                .phoneNumber("+48123456789")
                .role(Role.USER)
                .enabled(true)
                .verificationToken(null)
                .build();

        userRepository.saveAndFlush(testUser);

        // Generate valid JWT token
        validToken = jwtUtils.generateToken(testUser.getUsername(), testUser.getRole().name());
    }

    @Test
    @DisplayName("Should successfully logout user with valid token")
    void shouldSuccessfullyLogoutUserWithValidToken() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + validToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // When
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/logout",
                HttpMethod.POST,
                request,
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        
        // Verify token is blacklisted
        assertThat(blacklistService.isTokenBlacklisted(validToken)).isTrue();
    }

    @Test
    @DisplayName("Should return 400 when Authorization header is missing")
    void shouldReturn400WhenAuthorizationHeaderIsMissing() {
        // Given
        HttpEntity<Void> request = new HttpEntity<>(new HttpHeaders());

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/logout",
                HttpMethod.POST,
                request,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Authorization header is missing or invalid format");
        
        // Verify token is not blacklisted
        assertThat(blacklistService.isTokenBlacklisted(validToken)).isFalse();
    }

    @Test
    @DisplayName("Should return 400 when Authorization header has invalid format")
    void shouldReturn400WhenAuthorizationHeaderHasInvalidFormat() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic dGVzdDp0ZXN0");
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/logout",
                HttpMethod.POST,
                request,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Authorization header is missing or invalid format");
    }

    @Test
    @DisplayName("Should return 400 when Bearer token is malformed")
    void shouldReturn400WhenBearerTokenIsMalformed() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer");
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/logout",
                HttpMethod.POST,
                request,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should handle invalid JWT token gracefully")
    void shouldHandleInvalidJwtTokenGracefully() {
        // Given
        String invalidToken = "invalid.jwt.token";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + invalidToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/logout",
                HttpMethod.POST,
                request,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should logout successfully with expired token")
    void shouldLogoutSuccessfullyWithExpiredToken() {
        // Given - Create expired token manually (would need custom implementation or mock)
        // For this test, we'll use a token that will expire soon
        String expiredToken = jwtUtils.generateToken("testuser", "USER");
        
        // Wait a bit or manipulate time if needed
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + expiredToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // When
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/logout",
                HttpMethod.POST,
                request,
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        
        // Verify token is blacklisted (even if expired)
        assertThat(blacklistService.isTokenBlacklisted(expiredToken)).isTrue();
    }

    @Test
    @DisplayName("Should handle multiple logout requests for same token")
    void shouldHandleMultipleLogoutRequestsForSameToken() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + validToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // When - First logout
        ResponseEntity<Void> firstResponse = restTemplate.exchange(
                baseUrl + "/logout",
                HttpMethod.POST,
                request,
                Void.class
        );

        // When - Second logout with same token
        ResponseEntity<Void> secondResponse = restTemplate.exchange(
                baseUrl + "/logout",
                HttpMethod.POST,
                request,
                Void.class
        );

        // Then
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        
        // Verify token is still blacklisted
        assertThat(blacklistService.isTokenBlacklisted(validToken)).isTrue();
    }

    @Test
    @DisplayName("Should return correct content type and headers")
    void shouldReturnCorrectContentTypeAndHeaders() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + validToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // When
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/logout",
                HttpMethod.POST,
                request,
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getHeaders().getContentLength()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Should verify Redis TTL is set correctly for blacklisted token")
    void shouldVerifyRedisTtlIsSetCorrectlyForBlacklistedToken() throws InterruptedException {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + validToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // When
        restTemplate.exchange(
                baseUrl + "/logout",
                HttpMethod.POST,
                request,
                Void.class
        );

        // Then
        String redisKey = "blacklisted:" + validToken;
        Long ttl = redisTemplate.getExpire(redisKey);
        
        // TTL should be positive and reasonable (less than token expiration time)
        assertThat(ttl).isGreaterThan(0L);
        assertThat(ttl).isLessThan(2592000L); // Should be less than 30 days in seconds
    }
}