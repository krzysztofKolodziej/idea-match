package com.example.idea_match.idea.controller;


import com.example.idea_match.idea.command.AddIdeaCommand;
import com.example.idea_match.idea.command.UpdateIdeaCommand;
import com.example.idea_match.idea.dto.IdeaDetailsDto;
import com.example.idea_match.idea.dto.IdeaDto;
import com.example.idea_match.idea.service.IdeaService;
import com.example.idea_match.shared.filter.PaginationRequest;
import com.example.idea_match.shared.security.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("ideas/{id}")
    public ResponseEntity<IdeaDetailsDto> getIdeaDetails(@PathVariable Long id) {
        IdeaDetailsDto ideaDetails = ideaService.getIdeaDetails(id);
        return ResponseEntity.ok(ideaDetails);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("account/idea")
    public ResponseEntity<Void> addIdea(@RequestBody @Valid AddIdeaCommand ideaCommand,
                                        @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        ideaService.addIdea(ideaCommand, customUserDetails.getUser());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('USER')")
    @PatchMapping("account/idea/{id}")
    public ResponseEntity<Void> updateIdea(@PathVariable Long id,
                                          @RequestBody @Valid UpdateIdeaCommand updateCommand,
                                          @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        ideaService.updateIdea(id, updateCommand, customUserDetails.getUser());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("account/idea/{id}")
    public ResponseEntity<Void> deleteIdea(@PathVariable Long id,
                                          @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        ideaService.deleteIdea(id, customUserDetails.getUser());
        return ResponseEntity.noContent().build();
    }


}
