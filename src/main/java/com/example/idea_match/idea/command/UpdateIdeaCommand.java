package com.example.idea_match.idea.command;

import com.example.idea_match.idea.model.IdeaCategory;
import com.example.idea_match.idea.model.IdeaStatus;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateIdeaCommand(
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,
        
        @Size(max = 200, message = "Location must not exceed 200 characters")
        String location,
        
        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,
        
        @Size(max = 1000, message = "Goal must not exceed 1000 characters")
        String goal,
        
        IdeaStatus status,
        
        IdeaCategory category,
        
        LocalDateTime expectedStartDate
) {
}