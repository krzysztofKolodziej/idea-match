package com.example.idea_match.chat.command;

import com.example.idea_match.chat.model.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SendMessageCommand(
        @NotBlank(message = "Message content cannot be empty")
        @Size(max = 1000, message = "Message content cannot exceed 1000 characters")
        String content,
        
        @NotBlank(message = "Recipient ID is required")
        String recipientId,
        
        @NotNull(message = "Message type is required")
        MessageType messageType
) {
}