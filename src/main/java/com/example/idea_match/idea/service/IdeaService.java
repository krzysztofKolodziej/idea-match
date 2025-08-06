package com.example.idea_match.idea.service;

import com.example.idea_match.idea.command.AddIdeaCommand;
import com.example.idea_match.idea.command.UpdateIdeaCommand;
import com.example.idea_match.idea.dto.IdeaDetailsDto;
import com.example.idea_match.idea.dto.IdeaDto;
import com.example.idea_match.idea.exceptions.IdeaAccessDeniedException;
import com.example.idea_match.idea.exceptions.IdeaNotFoundException;
import com.example.idea_match.idea.model.Idea;
import com.example.idea_match.idea.repository.IdeaRepository;
import com.example.idea_match.shared.filter.PaginationRequest;
import com.example.idea_match.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.idea_match.shared.filter.RSQLPredicates.PREDICATES;
import static io.github.perplexhub.rsql.RSQLJPASupport.toSort;
import static io.github.perplexhub.rsql.RSQLJPASupport.toSpecification;

@RequiredArgsConstructor
@Service
public class IdeaService {

    private final IdeaRepository ideaRepository;
    private final IdeaMapper ideaMapper;

    @Transactional(readOnly = true)
    public PagedModel<IdeaDto> getAllIdeas(PaginationRequest request) {
        final Specification<Idea> sortSpecification = toSort(request.sort());
        final Specification<Idea> filterSpecification = toSpecification(request.filter(), PREDICATES);

        Page<Idea> page = ideaRepository.findAll(filterSpecification.and(sortSpecification), request.getPageable());

        List<IdeaDto> ideas = page.stream()
                .map(ideaMapper::toDto)
                .toList();

        PagedModel.PageMetadata metadata = getPageMetadata(page);

        return PagedModel.of(ideas, metadata);
    }

    @Transactional(readOnly = true)
    public IdeaDetailsDto getIdeaDetails(Long id) {
        Idea idea = ideaRepository.findById(id)
                .orElseThrow(IdeaNotFoundException::new);

        return ideaMapper.toDtoWithDetails(idea);
    }

    @Transactional
    public void addIdea(AddIdeaCommand command, User owner) {
        Idea idea = ideaMapper.toEntity(command, owner);
        ideaRepository.save(idea);
    }

    @Transactional
    public void updateIdea(Long ideaId, UpdateIdeaCommand command, User currentUser) {
        Idea idea = ideaRepository.findById(ideaId)
                .orElseThrow(IdeaNotFoundException::new);

        if (!idea.getOwner().getId().equals(currentUser.getId())) {
            throw new IdeaAccessDeniedException();
        }

        ideaMapper.updateEntity(command, idea);
        ideaRepository.save(idea);
    }

    @Transactional
    public void deleteIdea(Long ideaId, User currentUser) {
        Idea idea = ideaRepository.findById(ideaId)
                .orElseThrow(IdeaNotFoundException::new);

        if (!idea.getOwner().getId().equals(currentUser.getId())) {
            throw new IdeaAccessDeniedException();
        }

        ideaRepository.delete(idea);
    }

    private static PagedModel.PageMetadata getPageMetadata(Page<Idea> page) {
        return new PagedModel.PageMetadata(
                page.getSize(),
                page.getNumber(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
