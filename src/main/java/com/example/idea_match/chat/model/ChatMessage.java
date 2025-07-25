package com.example.idea_match.chat.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "chat_messages")
public class ChatMessage {

    @Id
    private String id;

    @Field("content")
    private String content;

    @Field("sender_id")
    private String senderId;

    @Field("sender_username")
    private String senderUsername;

    @Field("recipient_id")
    @Indexed
    private String recipientId;

    @Field("message_type")
    private MessageType messageType;

    @Field("status")
    @Builder.Default
    private MessageStatus status = MessageStatus.SENT;

    @Field("sent_at")
    @Indexed
    private LocalDateTime sentAt;

    @Field("read_at")
    private LocalDateTime readAt;

    @Field("delivered_at")
    private LocalDateTime deliveredAt;

    @Field("deleted")
    @Builder.Default
    private boolean deleted = false;

    @Field("session_id")
    private String sessionId;
}