package com.example.idea_match.idea.dto;

import com.example.idea_match.idea.model.IdeaCategory;
import com.example.idea_match.idea.model.IdeaStatus;

import java.time.LocalDateTime;

public record IdeaDetailsDto(
        Long id,
        String title,
        String location,
        String description,
        String goal,
        IdeaStatus status,
        IdeaCategory category,
        String username,
        LocalDateTime cratedDate,
        LocalDateTime expectedStartDate
) {
}
