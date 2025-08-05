package com.example.idea_match.idea.service;

import com.example.idea_match.idea.dto.IdeaDto;
import com.example.idea_match.idea.model.Idea;
import com.example.idea_match.idea.repository.IdeaRepository;
import com.example.idea_match.shared.filter.PaginationRequest;
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

    private static PagedModel.PageMetadata getPageMetadata(Page<Idea> page) {
        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(
                page.getSize(),
                page.getNumber(),
                page.getTotalElements(),
                page.getTotalPages()
        );
        return metadata;
    }
}
