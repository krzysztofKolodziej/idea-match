package com.example.idea_match.idea.controller;


import com.example.idea_match.idea.dto.IdeaDto;
import com.example.idea_match.idea.service.IdeaService;
import com.example.idea_match.shared.filter.PaginationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("api/")
@RestController
public class IdeaController {

    private final IdeaService ideaService;

    @GetMapping("ideas")
    public ResponseEntity<PagedModel<IdeaDto>> getAllIdeas(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "") String sort,
            @RequestParam(required = false, defaultValue = "") String filter
    ) {
        PaginationRequest request = new PaginationRequest(page, size, sort, filter);

        PagedModel<IdeaDto> ideas = ideaService.getAllIdeas(request);

        return ResponseEntity.ok(ideas);
    }



}
