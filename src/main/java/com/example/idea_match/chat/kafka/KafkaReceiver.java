package com.example.idea_match.chat.kafka;

import com.example.idea_match.chat.dto.ChatMessageResponse;
import com.example.idea_match.chat.mapper.ChatMessageMapper;
import com.example.idea_match.chat.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class KafkaReceiver {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatMessageMapper chatMessageMapper;

    @KafkaListener(topics = "messaging", groupId = "chat-fresh-start")
    public void consume(ChatMessage chatMessage) {
        ChatMessageResponse response = chatMessageMapper.toResponse(chatMessage);
        distributeMessage(response);
    }

    private void distributeMessage(ChatMessageResponse messageResponse) {
        String recipientId = messageResponse.getRecipientId();
        String senderId = messageResponse.getSenderUsername();

        messagingTemplate.convertAndSend(
                "/queue/messages-" + recipientId, messageResponse);

        messagingTemplate.convertAndSend(
                "/queue/messages-" + senderId, messageResponse);
    }
}
