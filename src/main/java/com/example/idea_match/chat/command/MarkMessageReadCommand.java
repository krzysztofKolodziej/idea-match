package com.example.idea_match.chat.command;

import jakarta.validation.constraints.NotBlank;

public record MarkMessageReadCommand(
        @NotBlank(message = "Message ID is required")
        String messageId
) {
}