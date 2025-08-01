package com.example.idea_match.chat.service;

import com.example.idea_match.chat.dto.ChatMessageResponse;
import com.example.idea_match.chat.exceptions.NotFoundMessageException;
import com.example.idea_match.chat.mapper.ChatMessageMapper;
import com.example.idea_match.chat.model.ChatMessage;
import com.example.idea_match.chat.model.MessageStatus;
import com.example.idea_match.chat.model.MessageType;
import com.example.idea_match.chat.repository.ChatMessageRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageStatusService Unit Tests")
class MessageStatusServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatMessageMapper chatMessageMapper;

    @Mock
    private SimpMessageSendingOperations messagingTemplate;

    @InjectMocks
    private MessageStatusService messageStatusService;

    private ChatMessage testMessage;
    private ChatMessageResponse testResponse;

    @BeforeEach
    void setUp() {
        testMessage = new ChatMessage();
        testMessage.setId("message-id");
        testMessage.setSenderId("sender-id");
        testMessage.setRecipientId("recipient-id");
        testMessage.setContent("Test message");
        testMessage.setStatus(MessageStatus.DELIVERED);
        testMessage.setMessageType(MessageType.TEXT);
        testMessage.setSentAt(LocalDateTime.now());

        testResponse = new ChatMessageResponse();
        testResponse.setId("message-id");
        testResponse.setSenderId("sender-id");
        testResponse.setRecipientId("recipient-id");
        testResponse.setContent("Test message");
        testResponse.setStatus(MessageStatus.READ);
    }

    @Test
    @DisplayName("Should mark message as read successfully")
    void shouldMarkMessageAsReadSuccessfully() {
        when(chatMessageRepository.findById("message-id"))
                .thenReturn(Optional.of(testMessage));
        when(chatMessageRepository.save(any(ChatMessage.class)))
                .thenReturn(testMessage);
        when(chatMessageMapper.toResponse(any(ChatMessage.class)))
                .thenReturn(testResponse);

        messageStatusService.markMessageAsRead("message-id", "recipient-id");

        ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatMessageRepository).save(messageCaptor.capture());
        
        ChatMessage savedMessage = messageCaptor.getValue();
        assertThat(savedMessage.getStatus()).isEqualTo(MessageStatus.READ);
        assertThat(savedMessage.getReadAt()).isNotNull();
        
        verify(messagingTemplate).convertAndSendToUser(
                eq("sender-id"),
                eq("/queue/status"),
                eq(testResponse)
        );
    }

    @Test
    @DisplayName("Should throw exception when message not found for read")
    void shouldThrowExceptionWhenMessageNotFoundForRead() {
        when(chatMessageRepository.findById("invalid-id"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageStatusService.markMessageAsRead("invalid-id", "recipient-id"))
                .isInstanceOf(NotFoundMessageException.class);

        verify(chatMessageRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any());
    }

    @Test
    @DisplayName("Should throw exception when user is not recipient")
    void shouldThrowExceptionWhenUserIsNotRecipient() {
        when(chatMessageRepository.findById("message-id"))
                .thenReturn(Optional.of(testMessage));

        assertThatThrownBy(() -> messageStatusService.markMessageAsRead("message-id", "wrong-user-id"))
                .isInstanceOf(NotFoundMessageException.class);

        verify(chatMessageRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any());
    }

    @Test
    @DisplayName("Should mark message as delivered successfully")
    void shouldMarkMessageAsDeliveredSuccessfully() {
        testMessage.setStatus(MessageStatus.SENT);
        
        when(chatMessageRepository.findById("message-id"))
                .thenReturn(Optional.of(testMessage));
        when(chatMessageRepository.save(any(ChatMessage.class)))
                .thenReturn(testMessage);
        when(chatMessageMapper.toResponse(any(ChatMessage.class)))
                .thenReturn(testResponse);

        messageStatusService.markMessageAsDelivered("message-id");

        ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatMessageRepository).save(messageCaptor.capture());
        
        ChatMessage savedMessage = messageCaptor.getValue();
        assertThat(savedMessage.getStatus()).isEqualTo(MessageStatus.DELIVERED);
        assertThat(savedMessage.getDeliveredAt()).isNotNull();
        
        verify(messagingTemplate).convertAndSendToUser(
                eq("sender-id"),
                eq("/queue/status"),
                eq(testResponse)
        );
    }

    @Test
    @DisplayName("Should throw exception when message not found for delivery")
    void shouldThrowExceptionWhenMessageNotFoundForDelivery() {
        when(chatMessageRepository.findById("invalid-id"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageStatusService.markMessageAsDelivered("invalid-id"))
                .isInstanceOf(NotFoundMessageException.class);

        verify(chatMessageRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any());
    }

    @Test
    @DisplayName("Should update delivery time when marking as delivered")
    void shouldUpdateDeliveryTimeWhenMarkingAsDelivered() {
        LocalDateTime beforeCall = LocalDateTime.now();
        testMessage.setStatus(MessageStatus.SENT);
        
        when(chatMessageRepository.findById("message-id"))
                .thenReturn(Optional.of(testMessage));
        when(chatMessageRepository.save(any(ChatMessage.class)))
                .thenReturn(testMessage);
        when(chatMessageMapper.toResponse(any(ChatMessage.class)))
                .thenReturn(testResponse);

        messageStatusService.markMessageAsDelivered("message-id");

        ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatMessageRepository).save(messageCaptor.capture());
        
        ChatMessage savedMessage = messageCaptor.getValue();
        LocalDateTime afterCall = LocalDateTime.now();
        
        assertThat(savedMessage.getDeliveredAt()).isNotNull();
        assertThat(savedMessage.getDeliveredAt()).isAfterOrEqualTo(beforeCall);
        assertThat(savedMessage.getDeliveredAt()).isBeforeOrEqualTo(afterCall);
    }

    @Test
    @DisplayName("Should update read time when marking as read")
    void shouldUpdateReadTimeWhenMarkingAsRead() {
        LocalDateTime beforeCall = LocalDateTime.now();
        
        when(chatMessageRepository.findById("message-id"))
                .thenReturn(Optional.of(testMessage));
        when(chatMessageRepository.save(any(ChatMessage.class)))
                .thenReturn(testMessage);
        when(chatMessageMapper.toResponse(any(ChatMessage.class)))
                .thenReturn(testResponse);

        messageStatusService.markMessageAsRead("message-id", "recipient-id");

        ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatMessageRepository).save(messageCaptor.capture());
        
        ChatMessage savedMessage = messageCaptor.getValue();
        LocalDateTime afterCall = LocalDateTime.now();
        
        assertThat(savedMessage.getReadAt()).isNotNull();
        assertThat(savedMessage.getReadAt()).isAfterOrEqualTo(beforeCall);
        assertThat(savedMessage.getReadAt()).isBeforeOrEqualTo(afterCall);
    }
}