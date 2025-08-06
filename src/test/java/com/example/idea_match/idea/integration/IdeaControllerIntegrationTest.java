package com.example.idea_match.idea.integration;

import com.example.idea_match.config.TestConfig;
import com.example.idea_match.idea.dto.IdeaDetailsDto;
import com.example.idea_match.idea.dto.IdeaDto;
import com.example.idea_match.idea.model.Idea;
import com.example.idea_match.idea.model.IdeaCategory;
import com.example.idea_match.idea.model.IdeaStatus;
import com.example.idea_match.idea.repository.IdeaRepository;
import com.example.idea_match.user.dto.AuthResponse;
import com.example.idea_match.user.dto.LoginRequest;
import com.example.idea_match.user.model.Role;
import com.example.idea_match.user.model.User;
import com.example.idea_match.user.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedModel;
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
@Import(TestConfig.class)
class IdeaControllerIntegrationTest {

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
    private IdeaRepository ideaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Idea testIdea1;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        ideaRepository.deleteAll();

        User testUser = User.builder()
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
        
        testUser = userRepository.save(testUser);

        testIdea1 = Idea.builder()
                .title("Mobile App for Local Events")
                .location("Warsaw")
                .description("A mobile application that helps people discover local events in their area")
                .goal("Create a user-friendly app with event discovery features")
                .status(IdeaStatus.ACTIVE)
                .category(IdeaCategory.TECHNOLOGY)
                .owner(testUser)
                .cratedDate(LocalDateTime.of(2023, 1, 1, 12, 0))
                .expectedStartDate(LocalDateTime.of(2023, 2, 1, 0, 0))
                .build();

        Idea testIdea2 = Idea.builder()
                .title("Sustainable Farming Initiative")
                .location("Krakow")
                .description("An initiative to promote sustainable farming practices in urban areas")
                .goal("Establish community gardens and educational programs")
                .status(IdeaStatus.ACTIVE)
                .category(IdeaCategory.TECHNOLOGY)
                .owner(testUser)
                .cratedDate(LocalDateTime.of(2023, 1, 15, 10, 30))
                .expectedStartDate(LocalDateTime.of(2023, 3, 1, 0, 0))
                .build();

        testIdea1 = ideaRepository.save(testIdea1);
        testIdea2 = ideaRepository.save(testIdea2);
    }

    @Test
    void shouldGetAllIdeasSuccessfully() {
        // when
        ResponseEntity<PagedModel<IdeaDto>> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/ideas",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PagedModel<IdeaDto>>() {}
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        PagedModel<IdeaDto> pagedModel = response.getBody();
        assertThat(pagedModel.getContent()).hasSize(2);
        Assertions.assertNotNull(pagedModel.getMetadata());
        assertThat(pagedModel.getMetadata().getSize()).isEqualTo(10);
        assertThat(pagedModel.getMetadata().getNumber()).isEqualTo(0);
        assertThat(pagedModel.getMetadata().getTotalElements()).isEqualTo(2);
        assertThat(pagedModel.getMetadata().getTotalPages()).isEqualTo(1);

        // Verify idea details
        IdeaDto[] ideas = pagedModel.getContent().toArray(new IdeaDto[0]);
        assertThat(ideas[0].title()).isEqualTo("Mobile App for Local Events");
        assertThat(ideas[0].location()).isEqualTo("Warsaw");
        assertThat(ideas[0].category()).isEqualTo(IdeaCategory.TECHNOLOGY);
        assertThat(ideas[0].username()).isEqualTo("johndoe");
        
        assertThat(ideas[1].title()).isEqualTo("Sustainable Farming Initiative");
        assertThat(ideas[1].location()).isEqualTo("Krakow");
        assertThat(ideas[1].category()).isEqualTo(IdeaCategory.TECHNOLOGY);
        assertThat(ideas[1].username()).isEqualTo("johndoe");
    }

    @Test
    void shouldGetAllIdeasWithPaginationParameters() {
        // when
        ResponseEntity<PagedModel<IdeaDto>> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/ideas?page=0&size=1",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PagedModel<IdeaDto>>() {}
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        PagedModel<IdeaDto> pagedModel = response.getBody();
        assertThat(pagedModel.getContent()).hasSize(1);
        Assertions.assertNotNull(pagedModel.getMetadata());
        assertThat(pagedModel.getMetadata().getSize()).isEqualTo(1);
        assertThat(pagedModel.getMetadata().getNumber()).isEqualTo(0);
        assertThat(pagedModel.getMetadata().getTotalElements()).isEqualTo(2);
        assertThat(pagedModel.getMetadata().getTotalPages()).isEqualTo(2);
    }

    @Test
    void shouldGetIdeaDetailsSuccessfully() {
        // when
        ResponseEntity<IdeaDetailsDto> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/ideas/" + testIdea1.getId(),
                HttpMethod.GET,
                null,
                IdeaDetailsDto.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        IdeaDetailsDto ideaDetails = response.getBody();
        assertThat(ideaDetails.id()).isEqualTo(testIdea1.getId());
        assertThat(ideaDetails.title()).isEqualTo("Mobile App for Local Events");
        assertThat(ideaDetails.location()).isEqualTo("Warsaw");
        assertThat(ideaDetails.description()).isEqualTo("A mobile application that helps people discover local events in their area");
        assertThat(ideaDetails.goal()).isEqualTo("Create a user-friendly app with event discovery features");
        assertThat(ideaDetails.status()).isEqualTo(IdeaStatus.ACTIVE);
        assertThat(ideaDetails.category()).isEqualTo(IdeaCategory.TECHNOLOGY);
        assertThat(ideaDetails.username()).isEqualTo("johndoe");
        assertThat(ideaDetails.cratedDate()).isEqualTo(LocalDateTime.of(2023, 1, 1, 12, 0));
    }

    @Test
    void shouldReturnNotFoundWhenIdeaDoesNotExist() {
        // when
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/ideas/999",
                HttpMethod.GET,
                null,
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldGetEmptyPageWhenNoIdeasExist() {
        // given - clean up all ideas
        ideaRepository.deleteAll();

        // when
        ResponseEntity<PagedModel<IdeaDto>> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/ideas",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PagedModel<IdeaDto>>() {}
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        PagedModel<IdeaDto> pagedModel = response.getBody();
        assertThat(pagedModel.getContent()).isEmpty();
        Assertions.assertNotNull(pagedModel.getMetadata());
        assertThat(pagedModel.getMetadata().getTotalElements()).isEqualTo(0);
        assertThat(pagedModel.getMetadata().getTotalPages()).isEqualTo(0);
    }

    @Test
    void shouldGetAllIdeasWithFilterAndSortParameters() {
        // when - test with filter and sort parameters (even if they might not be fully implemented)
        ResponseEntity<PagedModel<IdeaDto>> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/ideas?page=0&size=10&sort=title&filter=status==ACTIVE",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PagedModel<IdeaDto>>() {}
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
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