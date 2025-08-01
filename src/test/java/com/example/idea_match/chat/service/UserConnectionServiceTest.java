package com.example.idea_match.chat.service;

import com.example.idea_match.chat.dto.ChatMessageResponse;
import com.example.idea_match.chat.model.MessageStatus;
import com.example.idea_match.chat.model.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserConnectionService Unit Tests")
class UserConnectionServiceTest {

    @Mock
    private SimpMessageSendingOperations messagingTemplate;

    @InjectMocks
    private UserConnectionService userConnectionService;

    private final String testUsername = "testuser";

    @Test
    @DisplayName("Should handle user connect successfully")
    void shouldHandleUserConnectSuccessfully() {
        LocalDateTime beforeCall = LocalDateTime.now();

        userConnectionService.handleUserConnect(testUsername);

        ArgumentCaptor<ChatMessageResponse> responseCaptor = ArgumentCaptor.forClass(ChatMessageResponse.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/public"), responseCaptor.capture());

        ChatMessageResponse capturedResponse = responseCaptor.getValue();
        LocalDateTime afterCall = LocalDateTime.now();

        assertThat(capturedResponse.getSenderUsername()).isEqualTo(testUsername);
        assertThat(capturedResponse.getMessageType()).isEqualTo(MessageType.CONNECT);
        assertThat(capturedResponse.getStatus()).isEqualTo(MessageStatus.SENT);
        assertThat(capturedResponse.getSentAt()).isNotNull();
        assertThat(capturedResponse.getSentAt()).isAfterOrEqualTo(beforeCall);
        assertThat(capturedResponse.getSentAt()).isBeforeOrEqualTo(afterCall);
    }

    @Test
    @DisplayName("Should handle user disconnect successfully")
    void shouldHandleUserDisconnectSuccessfully() {
        LocalDateTime beforeCall = LocalDateTime.now();

        userConnectionService.handleUserDisconnect(testUsername);

        ArgumentCaptor<ChatMessageResponse> responseCaptor = ArgumentCaptor.forClass(ChatMessageResponse.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/public"), responseCaptor.capture());

        ChatMessageResponse capturedResponse = responseCaptor.getValue();
        LocalDateTime afterCall = LocalDateTime.now();

        assertThat(capturedResponse.getSenderUsername()).isEqualTo(testUsername);
        assertThat(capturedResponse.getMessageType()).isEqualTo(MessageType.DISCONNECT);
        assertThat(capturedResponse.getStatus()).isEqualTo(MessageStatus.SENT);
        assertThat(capturedResponse.getSentAt()).isNotNull();
        assertThat(capturedResponse.getSentAt()).isAfterOrEqualTo(beforeCall);
        assertThat(capturedResponse.getSentAt()).isBeforeOrEqualTo(afterCall);
    }

    @Test
    @DisplayName("Should send connect message to public topic")
    void shouldSendConnectMessageToPublicTopic() {
        userConnectionService.handleUserConnect(testUsername);

        ArgumentCaptor<ChatMessageResponse> responseCaptor = ArgumentCaptor.forClass(ChatMessageResponse.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/public"), responseCaptor.capture());
        
        assertThat(responseCaptor.getValue()).isNotNull();
    }

    @Test
    @DisplayName("Should send disconnect message to public topic")
    void shouldSendDisconnectMessageToPublicTopic() {
        userConnectionService.handleUserDisconnect(testUsername);

        ArgumentCaptor<ChatMessageResponse> responseCaptor = ArgumentCaptor.forClass(ChatMessageResponse.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/public"), responseCaptor.capture());
        
        assertThat(responseCaptor.getValue()).isNotNull();
    }

    @Test
    @DisplayName("Should create different message types for connect and disconnect")
    void shouldCreateDifferentMessageTypesForConnectAndDisconnect() {
        userConnectionService.handleUserConnect(testUsername);
        userConnectionService.handleUserDisconnect(testUsername);

        ArgumentCaptor<ChatMessageResponse> responseCaptor = ArgumentCaptor.forClass(ChatMessageResponse.class);
        verify(messagingTemplate, times(2)).convertAndSend(eq("/topic/public"), responseCaptor.capture());

        java.util.List<ChatMessageResponse> capturedResponses = responseCaptor.getAllValues();
        
        assertThat(capturedResponses.get(0).getMessageType()).isEqualTo(MessageType.CONNECT);
        assertThat(capturedResponses.get(1).getMessageType()).isEqualTo(MessageType.DISCONNECT);
    }

    @Test
    @DisplayName("Should handle null username gracefully")
    void shouldHandleNullUsernameGracefully() {
        userConnectionService.handleUserConnect(null);

        ArgumentCaptor<ChatMessageResponse> responseCaptor = ArgumentCaptor.forClass(ChatMessageResponse.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/public"), responseCaptor.capture());

        ChatMessageResponse capturedResponse = responseCaptor.getValue();
        assertThat(capturedResponse.getSenderUsername()).isNull();
        assertThat(capturedResponse.getMessageType()).isEqualTo(MessageType.CONNECT);
    }

    @Test
    @DisplayName("Should handle empty username gracefully")
    void shouldHandleEmptyUsernameGracefully() {
        userConnectionService.handleUserConnect("");

        ArgumentCaptor<ChatMessageResponse> responseCaptor = ArgumentCaptor.forClass(ChatMessageResponse.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/public"), responseCaptor.capture());

        ChatMessageResponse capturedResponse = responseCaptor.getValue();
        assertThat(capturedResponse.getSenderUsername()).isEmpty();
        assertThat(capturedResponse.getMessageType()).isEqualTo(MessageType.CONNECT);
    }

}