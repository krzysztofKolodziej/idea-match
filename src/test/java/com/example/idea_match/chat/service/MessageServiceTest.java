package com.example.idea_match.chat.service;

import com.example.idea_match.chat.dto.ChatMessageResponse;
import com.example.idea_match.chat.dto.SendMessageRequest;
import com.example.idea_match.chat.exceptions.ChatMessageException;
import com.example.idea_match.chat.kafka.KafkaSender;
import com.example.idea_match.chat.mapper.ChatMessageMapper;
import com.example.idea_match.chat.model.ChatMessage;
import com.example.idea_match.chat.model.MessageStatus;
import com.example.idea_match.chat.model.MessageType;
import com.example.idea_match.chat.repository.ChatMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageService Unit Tests")
class MessageServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatMessageMapper chatMessageMapper;

    @Mock
    private KafkaSender kafkaSender;

    @InjectMocks
    private MessageService messageService;

    private SendMessageRequest sendMessageRequest;
    private ChatMessage testMessage;

    @BeforeEach
    void setUp() {
        sendMessageRequest = new SendMessageRequest();
        sendMessageRequest.setRecipientId("recipient-id");
        sendMessageRequest.setContent("Test message content");

        testMessage = new ChatMessage();
        testMessage.setId("message-id");
        testMessage.setSenderId("sender-id");
        testMessage.setRecipientId("recipient-id");
        testMessage.setContent("Test message content");
        testMessage.setStatus(MessageStatus.SENT);
        testMessage.setMessageType(MessageType.TEXT);
        testMessage.setSentAt(LocalDateTime.now());

        ChatMessageResponse testResponse = new ChatMessageResponse();
        testResponse.setId("message-id");
        testResponse.setSenderId("sender-id");
        testResponse.setRecipientId("recipient-id");
        testResponse.setContent("Test message content");
        testResponse.setStatus(MessageStatus.SENT);
    }

    @Test
    @DisplayName("Should send message successfully")
    void shouldSendMessageSuccessfully() {
        when(chatMessageMapper.toEntity(sendMessageRequest, "sender-id", "senderUsername"))
                .thenReturn(testMessage);
        when(chatMessageRepository.save(testMessage))
                .thenReturn(testMessage);

        messageService.sendMessage(sendMessageRequest, "sender-id", "senderUsername");

        verify(chatMessageMapper).toEntity(sendMessageRequest, "sender-id", "senderUsername");
        verify(chatMessageRepository).save(testMessage);
        verify(kafkaSender).send("messaging", testMessage);
    }

    @Test
    @DisplayName("Should throw exception when mapper returns null")
    void shouldThrowExceptionWhenMapperReturnsNull() {
        when(chatMessageMapper.toEntity(sendMessageRequest, "sender-id", "senderUsername"))
                .thenReturn(null);

        assertThatThrownBy(() -> 
                messageService.sendMessage(sendMessageRequest, "sender-id", "senderUsername"))
                .isInstanceOf(ChatMessageException.class);

        verify(chatMessageRepository, never()).save(any());
        verify(kafkaSender, never()).send(any(), any());
    }

    @Test
    @DisplayName("Should get unread messages successfully")
    void shouldGetUnreadMessagesSuccessfully() {
        ChatMessage unreadMessage1 = createUnreadMessage("msg1", "Hello");
        ChatMessage unreadMessage2 = createUnreadMessage("msg2", "How are you?");
        List<ChatMessage> unreadMessages = Arrays.asList(unreadMessage1, unreadMessage2);

        ChatMessageResponse response1 = createResponse("msg1", "Hello");
        ChatMessageResponse response2 = createResponse("msg2", "How are you?");
        List<ChatMessageResponse> expectedResponses = Arrays.asList(response1, response2);

        when(chatMessageRepository.findUnreadMessages("user-id"))
                .thenReturn(unreadMessages);
        when(chatMessageMapper.toResponseList(unreadMessages))
                .thenReturn(expectedResponses);

        List<ChatMessageResponse> result = messageService.getUnreadMessages("user-id");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo("Hello");
        assertThat(result.get(1).getContent()).isEqualTo("How are you?");

        verify(chatMessageRepository).findUnreadMessages("user-id");
        verify(chatMessageMapper).toResponseList(unreadMessages);
    }

    @Test
    @DisplayName("Should return empty list when no unread messages")
    void shouldReturnEmptyListWhenNoUnreadMessages() {
        when(chatMessageRepository.findUnreadMessages("user-id"))
                .thenReturn(List.of());
        when(chatMessageMapper.toResponseList(any()))
                .thenReturn(List.of());

        List<ChatMessageResponse> result = messageService.getUnreadMessages("user-id");

        assertThat(result).isEmpty();
        verify(chatMessageRepository).findUnreadMessages("user-id");
    }

    @Test
    @DisplayName("Should handle kafka sending after successful save")
    void shouldHandleKafkaSendingAfterSuccessfulSave() {
        when(chatMessageMapper.toEntity(sendMessageRequest, "sender-id", "senderUsername"))
                .thenReturn(testMessage);
        when(chatMessageRepository.save(testMessage))
                .thenReturn(testMessage);

        messageService.sendMessage(sendMessageRequest, "sender-id", "senderUsername");

        verify(chatMessageRepository).save(testMessage);
        verify(kafkaSender).send(eq("messaging"), eq(testMessage));
    }

    @Test
    @DisplayName("Should use correct topic for kafka message")
    void shouldUseCorrectTopicForKafkaMessage() {
        when(chatMessageMapper.toEntity(sendMessageRequest, "sender-id", "senderUsername"))
                .thenReturn(testMessage);
        when(chatMessageRepository.save(testMessage))
                .thenReturn(testMessage);

        messageService.sendMessage(sendMessageRequest, "sender-id", "senderUsername");

        verify(kafkaSender).send("messaging", testMessage);
    }

    private ChatMessage createUnreadMessage(String id, String content) {
        ChatMessage message = new ChatMessage();
        message.setId(id);
        message.setSenderId("sender-id");
        message.setRecipientId("user-id");
        message.setContent(content);
        message.setStatus(MessageStatus.DELIVERED);
        message.setMessageType(MessageType.TEXT);
        message.setSentAt(LocalDateTime.now());
        return message;
    }

    private ChatMessageResponse createResponse(String id, String content) {
        ChatMessageResponse response = new ChatMessageResponse();
        response.setId(id);
        response.setSenderId("sender-id");
        response.setRecipientId("user-id");
        response.setContent(content);
        response.setStatus(MessageStatus.DELIVERED);
        return response;
    }
}