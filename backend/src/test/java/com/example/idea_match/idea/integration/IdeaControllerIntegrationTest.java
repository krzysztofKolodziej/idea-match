package com.example.idea_match.idea.integration;

import com.example.idea_match.config.TestConfig;
import com.example.idea_match.idea.command.AddIdeaCommand;
import com.example.idea_match.idea.command.UpdateIdeaCommand;
import com.example.idea_match.idea.dto.IdeaDetailsDto;
import com.example.idea_match.idea.dto.IdeaDto;
import com.example.idea_match.idea.model.Idea;
import com.example.idea_match.idea.model.IdeaCategory;
import com.example.idea_match.idea.model.IdeaStatus;
import com.example.idea_match.idea.repository.IdeaRepository;
import com.example.idea_match.user.dto.AuthResponse;
import com.example.idea_match.user.command.LoginCommand;
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
    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        ideaRepository.deleteAll();

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
        
        testUser = userRepository.save(testUser);

        testIdea1 = Idea.builder()
                .title("Mobile App for Local Events")
                .location("Warsaw")
                .description("A mobile application that helps people discover local events in their area")
                .goal("Create a user-friendly app with event discovery features")
                .status(IdeaStatus.ACTIVE)
                .category(IdeaCategory.TECHNOLOGY)
                .owner(testUser)
                .createdDate(LocalDateTime.of(2023, 1, 1, 12, 0))
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
                .createdDate(LocalDateTime.of(2023, 1, 15, 10, 30))
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
        assertThat(ideaDetails.createdDate()).isEqualTo(LocalDateTime.of(2023, 1, 1, 12, 0));
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
        LoginCommand loginRequest = new LoginCommand(usernameOrEmail, password);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginCommand> request = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/login", request, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return response.getBody().getToken();
    }

    @Test
    void shouldAddIdeaSuccessfully() {
        // given
        String jwtToken = authenticateAndGetJwtToken("johndoe", "password123");
        
        AddIdeaCommand addCommand = new AddIdeaCommand(
                "New Innovative App",
                "Poznan", 
                "An innovative application for modern problems",
                "Create something amazing",
                IdeaCategory.CREATIVE,
                LocalDateTime.of(2024, 8, 1, 10, 0)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken);
        HttpEntity<AddIdeaCommand> request = new HttpEntity<>(addCommand, headers);

        // when
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/account/idea", request, Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        // Verify idea was saved
        assertThat(ideaRepository.count()).isEqualTo(3); // 2 existing + 1 new
        
        Idea savedIdea = ideaRepository.findAll().stream()
                .filter(idea -> "New Innovative App".equals(idea.getTitle()))
                .findFirst()
                .orElse(null);
        
        assertThat(savedIdea).isNotNull();
        assertThat(savedIdea.getTitle()).isEqualTo("New Innovative App");
        assertThat(savedIdea.getLocation()).isEqualTo("Poznan");
        assertThat(savedIdea.getDescription()).isEqualTo("An innovative application for modern problems");
        assertThat(savedIdea.getGoal()).isEqualTo("Create something amazing");
        assertThat(savedIdea.getStatus()).isEqualTo(IdeaStatus.DRAFT);
        assertThat(savedIdea.getCategory()).isEqualTo(IdeaCategory.CREATIVE);
        assertThat(savedIdea.getOwner().getId()).isEqualTo(testUser.getId());
        assertThat(savedIdea.getExpectedStartDate()).isEqualTo(LocalDateTime.of(2024, 8, 1, 10, 0));
    }

    @Test
    void shouldReturnUnauthorizedWhenAddingIdeaWithoutToken() {
        // given
        AddIdeaCommand addCommand = new AddIdeaCommand(
                "New Idea",
                "Warsaw",
                "Description",
                "Goal",
                IdeaCategory.TECHNOLOGY,
                null
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AddIdeaCommand> request = new HttpEntity<>(addCommand, headers);

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/account/idea", request, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ideaRepository.count()).isEqualTo(2);
    }

    @Test
    void shouldReturnBadRequestWhenAddingIdeaWithInvalidData() {
        // given
        String jwtToken = authenticateAndGetJwtToken("johndoe", "password123");
        
        AddIdeaCommand invalidCommand = new AddIdeaCommand(
                "", // Empty title - should fail validation
                "",
                "",
                null,
                null,
                null
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken);
        HttpEntity<AddIdeaCommand> request = new HttpEntity<>(invalidCommand, headers);

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/account/idea", request, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ideaRepository.count()).isEqualTo(2); // No new ideas added
    }

    @Test
    void shouldUpdateIdeaSuccessfully() {
        // given
        String jwtToken = authenticateAndGetJwtToken("johndoe", "password123");
        
        UpdateIdeaCommand updateCommand = new UpdateIdeaCommand(
                "Updated Mobile App",
                "Gdansk",
                "Updated description for mobile app",
                "Updated goal",
                IdeaStatus.PAUSED,
                IdeaCategory.BUSINESS,
                LocalDateTime.of(2024, 9, 1, 14, 0)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken);
        HttpEntity<UpdateIdeaCommand> request = new HttpEntity<>(updateCommand, headers);

        // when
        ResponseEntity<Void> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/account/idea/" + testIdea1.getId(),
                HttpMethod.PATCH,
                request,
                Void.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        // Verify idea was updated
        Idea updatedIdea = ideaRepository.findById(testIdea1.getId()).orElse(null);
        assertThat(updatedIdea).isNotNull();
        assertThat(updatedIdea.getTitle()).isEqualTo("Updated Mobile App");
        assertThat(updatedIdea.getLocation()).isEqualTo("Gdansk");
        assertThat(updatedIdea.getDescription()).isEqualTo("Updated description for mobile app");
        assertThat(updatedIdea.getGoal()).isEqualTo("Updated goal");
        assertThat(updatedIdea.getStatus()).isEqualTo(IdeaStatus.PAUSED);
        assertThat(updatedIdea.getCategory()).isEqualTo(IdeaCategory.BUSINESS);
        assertThat(updatedIdea.getExpectedStartDate()).isEqualTo(LocalDateTime.of(2024, 9, 1, 14, 0));
        // Owner should remain unchanged
        assertThat(updatedIdea.getOwner().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void shouldUpdateIdeaPartiallySuccessfully() {
        // given
        String jwtToken = authenticateAndGetJwtToken("johndoe", "password123");
        
        UpdateIdeaCommand partialUpdateCommand = new UpdateIdeaCommand(
                "Partially Updated Title",
                null, // Keep existing location
                null, // Keep existing description
                null, // Keep existing goal
                IdeaStatus.COMPLETED,
                null, // Keep existing category
                null  // Keep existing expectedStartDate
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken);
        HttpEntity<UpdateIdeaCommand> request = new HttpEntity<>(partialUpdateCommand, headers);

        // when
        ResponseEntity<Void> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/account/idea/" + testIdea1.getId(),
                HttpMethod.PATCH,
                request,
                Void.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        // Verify only specified fields were updated
        Idea updatedIdea = ideaRepository.findById(testIdea1.getId()).orElse(null);
        assertThat(updatedIdea).isNotNull();
        assertThat(updatedIdea.getTitle()).isEqualTo("Partially Updated Title");
        assertThat(updatedIdea.getStatus()).isEqualTo(IdeaStatus.COMPLETED);
        // These should remain unchanged
        assertThat(updatedIdea.getLocation()).isEqualTo("Warsaw");
        assertThat(updatedIdea.getDescription()).isEqualTo("A mobile application that helps people discover local events in their area");
        assertThat(updatedIdea.getGoal()).isEqualTo("Create a user-friendly app with event discovery features");
        assertThat(updatedIdea.getCategory()).isEqualTo(IdeaCategory.TECHNOLOGY);
        assertThat(updatedIdea.getExpectedStartDate()).isEqualTo(LocalDateTime.of(2023, 2, 1, 0, 0));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentIdea() {
        // given
        String jwtToken = authenticateAndGetJwtToken("johndoe", "password123");
        
        UpdateIdeaCommand updateCommand = new UpdateIdeaCommand(
                "Updated Title", null, null, null, null, null, null
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken);
        HttpEntity<UpdateIdeaCommand> request = new HttpEntity<>(updateCommand, headers);

        // when
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/account/idea/999",
                HttpMethod.PATCH,
                request,
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnForbiddenWhenUpdatingIdeaNotOwnedByUser() {
        // given - create another user
        User anotherUser = User.builder()
                .firstName("Jane")
                .lastName("Smith")
                .username("janesmith")
                .email("jane@example.com")
                .phoneNumber("+48987654321")
                .password(passwordEncoder.encode("password456"))
                .enabled(true)
                .role(Role.USER)
                .build();
        userRepository.save(anotherUser);

        String anotherUserToken = authenticateAndGetJwtToken("janesmith", "password456");
        
        UpdateIdeaCommand updateCommand = new UpdateIdeaCommand(
                "Unauthorized Update", null, null, null, null, null, null
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(anotherUserToken);
        HttpEntity<UpdateIdeaCommand> request = new HttpEntity<>(updateCommand, headers);

        // when
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/account/idea/" + testIdea1.getId(),
                HttpMethod.PATCH,
                request,
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        
        // Verify idea was not updated
        Idea unchangedIdea = ideaRepository.findById(testIdea1.getId()).orElse(null);
        assertThat(unchangedIdea).isNotNull();
        assertThat(unchangedIdea.getTitle()).isEqualTo("Mobile App for Local Events"); // Original title
    }

    @Test
    void shouldDeleteIdeaSuccessfully() {
        // given
        String jwtToken = authenticateAndGetJwtToken("johndoe", "password123");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // when
        ResponseEntity<Void> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/account/idea/" + testIdea1.getId(),
                HttpMethod.DELETE,
                request,
                Void.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        
        // Verify idea was deleted
        assertThat(ideaRepository.findById(testIdea1.getId())).isEmpty();
        assertThat(ideaRepository.count()).isEqualTo(1); // Only 1 idea left
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentIdea() {
        // given
        String jwtToken = authenticateAndGetJwtToken("johndoe", "password123");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // when
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/account/idea/999",
                HttpMethod.DELETE,
                request,
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ideaRepository.count()).isEqualTo(2); // No ideas deleted
    }

    @Test
    void shouldReturnForbiddenWhenDeletingIdeaNotOwnedByUser() {
        // given - create another user
        User anotherUser = User.builder()
                .firstName("Alice")
                .lastName("Brown")
                .username("alicebrown")
                .email("alice@example.com")
                .phoneNumber("+48111222333")
                .password(passwordEncoder.encode("password789"))
                .enabled(true)
                .role(Role.USER)
                .build();
        userRepository.save(anotherUser);

        String anotherUserToken = authenticateAndGetJwtToken("alicebrown", "password789");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(anotherUserToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // when
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/account/idea/" + testIdea1.getId(),
                HttpMethod.DELETE,
                request,
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        
        // Verify idea was not deleted
        assertThat(ideaRepository.findById(testIdea1.getId())).isPresent();
        assertThat(ideaRepository.count()).isEqualTo(2); // No ideas deleted
    }

    @Test
    void shouldReturnUnauthorizedWhenAccessingProtectedEndpointsWithoutToken() {
        // Test POST endpoint
        AddIdeaCommand addCommand = new AddIdeaCommand("Title", "Location", "Description", "Goal", IdeaCategory.TECHNOLOGY, null);
        HttpEntity<AddIdeaCommand> postRequest = new HttpEntity<>(addCommand);
        
        ResponseEntity<String> postResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/account/idea", postRequest, String.class);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Test PATCH endpoint
        UpdateIdeaCommand updateCommand = new UpdateIdeaCommand("Title", null, null, null, null, null, null);
        HttpEntity<UpdateIdeaCommand> patchRequest = new HttpEntity<>(updateCommand);
        
        ResponseEntity<String> patchResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/account/idea/1", HttpMethod.PATCH, patchRequest, String.class);
        assertThat(patchResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Test DELETE endpoint
        ResponseEntity<String> deleteResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/account/idea/1", HttpMethod.DELETE, null, String.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}