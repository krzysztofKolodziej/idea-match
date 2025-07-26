package com.example.idea_match.chat.mapper;

import com.example.idea_match.chat.dto.ChatMessageResponse;
import com.example.idea_match.chat.dto.SendMessageRequest;
import com.example.idea_match.chat.model.ChatMessage;
import com.example.idea_match.chat.model.MessageStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", imports = {LocalDateTime.class, MessageStatus.class})
public interface ChatMessageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "senderId", source = "senderId")
    @Mapping(target = "senderUsername", source = "senderUsername")
    @Mapping(target = "content", source = "request.content")
    @Mapping(target = "recipientId", source = "request.recipientId")
    @Mapping(target = "messageType", source = "request.messageType")
    @Mapping(target = "sentAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "status", expression = "java(MessageStatus.SENT)")
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "readAt", ignore = true)
    @Mapping(target = "deliveredAt", ignore = true)
    ChatMessage toEntity(SendMessageRequest request, String senderUsername, String sessionId);

    ChatMessageResponse toResponse(ChatMessage entity);

    List<ChatMessageResponse> toResponseList(List<ChatMessage> entities);
}