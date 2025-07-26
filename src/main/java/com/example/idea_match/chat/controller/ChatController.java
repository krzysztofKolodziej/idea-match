package com.example.idea_match.chat.controller;

import com.example.idea_match.chat.dto.ChatMessageResponse;
import com.example.idea_match.chat.dto.SendMessageRequest;
import com.example.idea_match.chat.exceptions.UserNotAuthenticatedException;
import com.example.idea_match.chat.service.MessageService;
import com.example.idea_match.chat.service.MessageStatusService;
import com.example.idea_match.chat.service.UserConnectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/chat")
public class ChatController {

    private final MessageService messageService;
    private final MessageStatusService messageStatusService;
    private final UserConnectionService userConnectionService;

    @MessageMapping("/sendMessage")
    public void sendMessage(@Valid @Payload SendMessageRequest request, SimpMessageHeaderAccessor headerAccessor) {
        validateAuthenticated(headerAccessor);

        String username = Objects.requireNonNull(headerAccessor.getUser()).getName();
        String senderId = extractUserIdFromSession(headerAccessor);

        messageService.sendMessage(request, senderId, username);
    }

    @MessageMapping("/markAsRead")
    public void markMessageAsRead(@Payload String messageId, SimpMessageHeaderAccessor headerAccessor) {
        validateAuthenticated(headerAccessor);

        String userId = extractUserIdFromSession(headerAccessor);
        messageStatusService.markMessageAsRead(messageId, userId);
    }

    @MessageMapping("/markAsDelivered")
    public void markMessageAsDelivered(@Payload String messageId, SimpMessageHeaderAccessor headerAccessor) {
        validateAuthenticated(headerAccessor);

        messageStatusService.markMessageAsDelivered(messageId);
    }

    @MessageMapping("/connect")
    public void handleConnect(SimpMessageHeaderAccessor headerAccessor) {
        validateAuthenticated(headerAccessor);

        String username = Objects.requireNonNull(headerAccessor.getUser()).getName();

        userConnectionService.handleUserConnect(username);
    }

    @GetMapping("/unread")
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    public List<ChatMessageResponse> getUnreadMessages(@RequestParam String userId) {
        return messageService.getUnreadMessages(userId);
    }

    private String extractUserIdFromSession(SimpMessageHeaderAccessor headerAccessor) {
        return (String) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("userId");
    }

    private void validateAuthenticated(SimpMessageHeaderAccessor headerAccessor) {
        Boolean authenticated = (Boolean) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("authenticated");
        if (authenticated == null || !authenticated) {
            throw new UserNotAuthenticatedException();
        }
    }
}