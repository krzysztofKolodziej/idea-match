package com.example.idea_match.idea.service;

import com.example.idea_match.idea.command.AddIdeaCommand;
import com.example.idea_match.idea.command.UpdateIdeaCommand;
import com.example.idea_match.idea.dto.IdeaDetailsDto;
import com.example.idea_match.idea.dto.IdeaDto;
import com.example.idea_match.idea.exceptions.IdeaAccessDeniedException;
import com.example.idea_match.idea.exceptions.IdeaNotFoundException;
import com.example.idea_match.idea.model.Idea;
import com.example.idea_match.idea.model.IdeaCategory;
import com.example.idea_match.idea.model.IdeaStatus;
import com.example.idea_match.idea.repository.IdeaRepository;
import com.example.idea_match.shared.filter.PaginationRequest;
import com.example.idea_match.user.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.PagedModel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("IdeaService Unit Tests")
class IdeaServiceTest {

    @Mock
    private IdeaRepository ideaRepository;

    @Mock
    private IdeaMapper ideaMapper;

    @InjectMocks
    private IdeaService ideaService;

    private Idea testIdea;
    private IdeaDto testIdeaDto;
    private IdeaDetailsDto testIdeaDetailsDto;
    private User testUser;
    private User anotherUser;
    private AddIdeaCommand testAddCommand;
    private UpdateIdeaCommand testUpdateCommand;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        anotherUser = User.builder()
                .id(2L)
                .username("anotheruser")
                .build();

        testIdea = Idea.builder()
                .id(1L)
                .title("Test Idea")
                .location("Warsaw")
                .description("Test description")
                .goal("Test goal")
                .status(IdeaStatus.ACTIVE)
                .category(IdeaCategory.TECHNOLOGY)
                .owner(testUser)
                .createdDate(LocalDateTime.of(2023, 1, 1, 12, 0))
                .build();

        testAddCommand = new AddIdeaCommand(
                "New Test Idea",
                "Krakow",
                "New test description",
                "New test goal",
                IdeaCategory.BUSINESS,
                LocalDateTime.of(2024, 6, 1, 10, 0)
        );

        testUpdateCommand = new UpdateIdeaCommand(
                "Updated Test Idea",
                "Gdansk",
                "Updated test description",
                "Updated test goal",
                IdeaStatus.PAUSED,
                IdeaCategory.CREATIVE,
                LocalDateTime.of(2024, 7, 1, 14, 0)
        );

        testIdeaDto = new IdeaDto(
                1L,
                "Test Idea",
                "Warsaw",
                IdeaCategory.TECHNOLOGY,
                "testuser",
                LocalDateTime.of(2023, 1, 1, 12, 0)
        );

        testIdeaDetailsDto = new IdeaDetailsDto(
                1L,
                "Test Idea",
                "Warsaw",
                "Test description",
                "Test goal",
                IdeaStatus.ACTIVE,
                IdeaCategory.TECHNOLOGY,
                "testuser",
                LocalDateTime.of(2023, 1, 1, 12, 0),
                null
        );
    }

    @Test
    @DisplayName("Should get all ideas successfully")
    void shouldGetAllIdeasSuccessfully() {
        // given
        PaginationRequest request = new PaginationRequest(0, 10, null, null);
        Page<Idea> page = new PageImpl<>(List.of(testIdea), PageRequest.of(0, 10), 1);

        when(ideaRepository.findAll(any(Specification.class), eq(request.getPageable()))).thenReturn(page);
        when(ideaMapper.toDto(testIdea)).thenReturn(testIdeaDto);

        // when
        PagedModel<IdeaDto> result = ideaService.getAllIdeas(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().iterator().next()).isEqualTo(testIdeaDto);
        Assertions.assertNotNull(result.getMetadata());
        assertThat(result.getMetadata().getSize()).isEqualTo(10);
        assertThat(result.getMetadata().getNumber()).isEqualTo(0);
        assertThat(result.getMetadata().getTotalElements()).isEqualTo(1);
        assertThat(result.getMetadata().getTotalPages()).isEqualTo(1);

        verify(ideaRepository).findAll(any(Specification.class), eq(request.getPageable()));
        verify(ideaMapper).toDto(testIdea);
    }

    @Test
    @DisplayName("Should get idea details successfully when idea exists")
    void shouldGetIdeaDetailsSuccessfullyWhenIdeaExists() {
        // given
        Long ideaId = 1L;
        when(ideaRepository.findById(ideaId)).thenReturn(Optional.of(testIdea));
        when(ideaMapper.toDtoWithDetails(testIdea)).thenReturn(testIdeaDetailsDto);

        // when
        IdeaDetailsDto result = ideaService.getIdeaDetails(ideaId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("Test Idea");
        assertThat(result.location()).isEqualTo("Warsaw");
        assertThat(result.description()).isEqualTo("Test description");
        assertThat(result.goal()).isEqualTo("Test goal");
        assertThat(result.status()).isEqualTo(IdeaStatus.ACTIVE);
        assertThat(result.category()).isEqualTo(IdeaCategory.TECHNOLOGY);
        assertThat(result.username()).isEqualTo("testuser");

        verify(ideaRepository).findById(ideaId);
        verify(ideaMapper).toDtoWithDetails(testIdea);
    }

    @Test
    @DisplayName("Should throw IdeaNotFoundException when idea does not exist")
    void shouldThrowIdeaNotFoundExceptionWhenIdeaDoesNotExist() {
        // given
        Long nonExistentIdeaId = 999L;
        when(ideaRepository.findById(nonExistentIdeaId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> ideaService.getIdeaDetails(nonExistentIdeaId))
                .isInstanceOf(IdeaNotFoundException.class);

        verify(ideaRepository).findById(nonExistentIdeaId);
    }

    @Test
    @DisplayName("Should add idea successfully")
    void shouldAddIdeaSuccessfully() {
        // given
        Idea newIdea = Idea.builder()
                .title("New Test Idea")
                .location("Krakow")
                .description("New test description")
                .goal("New test goal")
                .status(IdeaStatus.DRAFT)
                .category(IdeaCategory.BUSINESS)
                .owner(testUser)
                .expectedStartDate(LocalDateTime.of(2024, 6, 1, 10, 0))
                .build();

        when(ideaMapper.toEntity(testAddCommand, testUser)).thenReturn(newIdea);
        when(ideaRepository.save(newIdea)).thenReturn(newIdea);

        // when
        ideaService.addIdea(testAddCommand, testUser);

        // then
        verify(ideaMapper).toEntity(testAddCommand, testUser);
        verify(ideaRepository).save(newIdea);
    }

    @Test
    @DisplayName("Should update idea successfully when user is owner")
    void shouldUpdateIdeaSuccessfullyWhenUserIsOwner() {
        // given
        Long ideaId = 1L;
        when(ideaRepository.findById(ideaId)).thenReturn(Optional.of(testIdea));
        when(ideaRepository.save(testIdea)).thenReturn(testIdea);

        // when
        ideaService.updateIdea(ideaId, testUpdateCommand, testUser);

        // then
        verify(ideaRepository).findById(ideaId);
        verify(ideaMapper).updateEntity(testUpdateCommand, testIdea);
        verify(ideaRepository).save(testIdea);
    }

    @Test
    @DisplayName("Should throw IdeaNotFoundException when updating non-existent idea")
    void shouldThrowIdeaNotFoundExceptionWhenUpdatingNonExistentIdea() {
        // given
        Long nonExistentIdeaId = 999L;
        when(ideaRepository.findById(nonExistentIdeaId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> ideaService.updateIdea(nonExistentIdeaId, testUpdateCommand, testUser))
                .isInstanceOf(IdeaNotFoundException.class);

        verify(ideaRepository).findById(nonExistentIdeaId);
        verifyNoInteractions(ideaMapper);
        verify(ideaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IdeaAccessDeniedException when updating idea not owned by user")
    void shouldThrowIdeaAccessDeniedExceptionWhenUpdatingIdeaNotOwnedByUser() {
        // given
        Long ideaId = 1L;
        when(ideaRepository.findById(ideaId)).thenReturn(Optional.of(testIdea));

        // when & then
        assertThatThrownBy(() -> ideaService.updateIdea(ideaId, testUpdateCommand, anotherUser))
                .isInstanceOf(IdeaAccessDeniedException.class);

        verify(ideaRepository).findById(ideaId);
        verifyNoInteractions(ideaMapper);
        verify(ideaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete idea successfully when user is owner")
    void shouldDeleteIdeaSuccessfullyWhenUserIsOwner() {
        // given
        Long ideaId = 1L;
        when(ideaRepository.findById(ideaId)).thenReturn(Optional.of(testIdea));

        // when
        ideaService.deleteIdea(ideaId, testUser);

        // then
        verify(ideaRepository).findById(ideaId);
        verify(ideaRepository).delete(testIdea);
    }

    @Test
    @DisplayName("Should throw IdeaNotFoundException when deleting non-existent idea")
    void shouldThrowIdeaNotFoundExceptionWhenDeletingNonExistentIdea() {
        // given
        Long nonExistentIdeaId = 999L;
        when(ideaRepository.findById(nonExistentIdeaId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> ideaService.deleteIdea(nonExistentIdeaId, testUser))
                .isInstanceOf(IdeaNotFoundException.class);

        verify(ideaRepository).findById(nonExistentIdeaId);
        verify(ideaRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw IdeaAccessDeniedException when deleting idea not owned by user")
    void shouldThrowIdeaAccessDeniedExceptionWhenDeletingIdeaNotOwnedByUser() {
        // given
        Long ideaId = 1L;
        when(ideaRepository.findById(ideaId)).thenReturn(Optional.of(testIdea));

        // when & then
        assertThatThrownBy(() -> ideaService.deleteIdea(ideaId, anotherUser))
                .isInstanceOf(IdeaAccessDeniedException.class);

        verify(ideaRepository).findById(ideaId);
        verify(ideaRepository, never()).delete(any());
    }
}