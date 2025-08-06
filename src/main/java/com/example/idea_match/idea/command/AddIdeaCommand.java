package com.example.idea_match.idea.command;

import com.example.idea_match.idea.model.IdeaCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AddIdeaCommand(
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,
        
        @NotBlank(message = "Location is required")
        @Size(max = 200, message = "Location must not exceed 200 characters")
        String location,
        
        @NotBlank(message = "Description is required")
        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,
        
        @Size(max = 1000, message = "Goal must not exceed 1000 characters")
        String goal,
        
        @NotNull(message = "Category is required")
        IdeaCategory category,
        
        LocalDateTime expectedStartDate
) {
}
