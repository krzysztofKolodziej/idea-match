package com.example.idea_match.chat.dto;

import com.example.idea_match.chat.model.MessageStatus;
import com.example.idea_match.chat.model.MessageType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageResponse {

    private String id;
    private String content;
    private String senderUsername;
    private String senderId;
    private String recipientId;
    private MessageType messageType;
    private MessageStatus status;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private LocalDateTime deliveredAt;
}