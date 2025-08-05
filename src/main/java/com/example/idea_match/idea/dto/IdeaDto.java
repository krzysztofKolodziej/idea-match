package com.example.idea_match.idea.dto;

import com.example.idea_match.idea.model.IdeaCategory;

import java.time.LocalDateTime;

public record IdeaDto(
        Long id,
        String title,
        String location,
        IdeaCategory category,
        String username,
        LocalDateTime cratedDate
) {
}
