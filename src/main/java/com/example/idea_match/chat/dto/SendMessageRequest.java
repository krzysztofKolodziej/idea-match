package com.example.idea_match.chat.dto;

import com.example.idea_match.chat.model.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendMessageRequest {

    @NotBlank(message = "Message content cannot be empty")
    @Size(max = 1000, message = "Message content cannot exceed 1000 characters")
    private String content;

    @NotBlank(message = "Recipient ID is required")
    private String recipientId;

    @NotNull(message = "Message type is required")
    private MessageType messageType = MessageType.TEXT;
}