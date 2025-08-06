package com.example.idea_match.idea.service;

import com.example.idea_match.idea.dto.IdeaDetailsDto;
import com.example.idea_match.idea.dto.IdeaDto;
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
import static org.mockito.Mockito.verify;
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

    @BeforeEach
    void setUp() {
        User testUser = User.builder()
                .id(1L)
                .username("testuser")
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
                .cratedDate(LocalDateTime.of(2023, 1, 1, 12, 0))
                .build();

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
}