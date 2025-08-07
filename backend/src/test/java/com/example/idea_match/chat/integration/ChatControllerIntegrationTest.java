package com.example.idea_match.chat.integration;

import com.example.idea_match.chat.dto.ChatMessageResponse;
import com.example.idea_match.chat.model.ChatMessage;
import com.example.idea_match.chat.model.MessageStatus;
import com.example.idea_match.chat.model.MessageType;
import com.example.idea_match.chat.repository.ChatMessageRepository;
import com.example.idea_match.config.TestConfig;
import com.example.idea_match.shared.security.jwt.JwtUtils;
import com.example.idea_match.user.model.User;
import com.example.idea_match.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(TestConfig.class)
class ChatControllerIntegrationTest {

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
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtils jwtUtils;

    private String testUserId;
    private String recipientId;
    private String validToken;
    private StompSession stompSession;
    private final BlockingQueue<String> messages = new LinkedBlockingQueue<>();

    @BeforeEach
    void setUp() throws Exception {
        User testUser = createTestUser("testuser", "test@example.com");
        User recipient = createTestUser("recipient", "recipient@example.com");

        Long testUserDbId = testUser.getId();
        Long recipientDbId = recipient.getId();
        testUserId = testUser.getId().toString();
        recipientId = recipient.getId().toString();
        
        // Generate valid JWT token for test user
        validToken = jwtUtils.generateToken(testUser.getUsername(), testUser.getRole().name());
        
        chatMessageRepository.deleteAll();
    }

    @Test
    void shouldGetUnreadMessages() {
        createTestMessage(testUserId, recipientId, "Hello", MessageStatus.SENT);
        createTestMessage(testUserId, recipientId, "How are you?", MessageStatus.DELIVERED);
        createTestMessage(recipientId, testUserId, "I'm good", MessageStatus.READ);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + validToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<ChatMessageResponse[]> response = restTemplate.exchange(
                "/api/chat/unread?userId=" + recipientId,
                HttpMethod.GET,
                entity,
                ChatMessageResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()[0].getContent()).isEqualTo("Hello");
        assertThat(response.getBody()[1].getContent()).isEqualTo("How are you?");
    }

    @Test
    void shouldReturnEmptyListWhenNoUnreadMessages() {
        createTestMessage(testUserId, recipientId, "Hello", MessageStatus.READ);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + validToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<ChatMessageResponse[]> response = restTemplate.exchange(
                "/api/chat/unread?userId=" + recipientId,
                HttpMethod.GET,
                entity,
                ChatMessageResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void shouldReturn403WhenUnauthorized() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/chat/unread?userId=" + recipientId,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private User createTestUser(String username, String email) {
        User user = new User();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setUsername(username);
        user.setEmail(email);
        user.setPhoneNumber("+123456789" + username.hashCode() % 10);
        user.setPassword("password");
        user.setRole(com.example.idea_match.user.model.Role.USER);
        return userRepository.save(user);
    }

    private ChatMessage createTestMessage(String senderId, String recipientId, String content, MessageStatus status) {
        ChatMessage message = new ChatMessage();
        message.setSenderId(senderId);
        message.setRecipientId(recipientId);
        message.setContent(content);
        message.setStatus(status);
        message.setMessageType(MessageType.TEXT);
        message.setSentAt(LocalDateTime.now());
        return chatMessageRepository.save(message);
    }

    private class TestStompSessionHandler extends StompSessionHandlerAdapter {
        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            exception.printStackTrace();
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return String.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            messages.offer((String) payload);
        }
    }
}