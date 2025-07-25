package com.example.idea_match.chat.service;

import com.example.idea_match.chat.dto.ChatMessageResponse;
import com.example.idea_match.chat.model.MessageStatus;
import com.example.idea_match.chat.model.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserConnectionService {

    private final SimpMessageSendingOperations messagingTemplate;

    public void handleUserConnect(String username) {
        ChatMessageResponse response = new ChatMessageResponse();
        response.setSenderUsername(username);
        response.setMessageType(MessageType.CONNECT);
        response.setStatus(MessageStatus.SENT);
        response.setSentAt(LocalDateTime.now());

        messagingTemplate.convertAndSend("/topic/public", response);
    }

    public void handleUserDisconnect(String username) {
        ChatMessageResponse response = new ChatMessageResponse();
        response.setSenderUsername(username);
        response.setMessageType(MessageType.DISCONNECT);
        response.setStatus(MessageStatus.SENT);
        response.setSentAt(LocalDateTime.now());

        messagingTemplate.convertAndSend("/topic/public", response);
    }
}