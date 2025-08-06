package com.example.idea_match.chat.service;

import com.example.idea_match.chat.command.MarkMessageDeliveredCommand;
import com.example.idea_match.chat.command.MarkMessageReadCommand;
import com.example.idea_match.chat.exceptions.NotFoundMessageException;
import com.example.idea_match.chat.mapper.ChatMessageMapper;
import com.example.idea_match.chat.model.ChatMessage;
import com.example.idea_match.chat.model.MessageStatus;
import com.example.idea_match.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MessageStatusService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageMapper chatMessageMapper;
    private final SimpMessageSendingOperations messagingTemplate;

    public void markMessageAsRead(MarkMessageReadCommand command, String userId) {
        ChatMessage chatMessage = chatMessageRepository.findById(command.messageId())
                .filter(message -> message.getRecipientId().equals(userId))
                .orElseThrow(NotFoundMessageException::new);

        chatMessage.setStatus(MessageStatus.READ);
        chatMessage.setReadAt(LocalDateTime.now());

        ChatMessage updatedMessage = chatMessageRepository.save(chatMessage);
        notifyMessageStatusUpdate(updatedMessage);
    }

    public void markMessageAsDelivered(MarkMessageDeliveredCommand command) {
        ChatMessage chatMessage = chatMessageRepository.findById(command.messageId())
                .orElseThrow(NotFoundMessageException::new);

        chatMessage.setStatus(MessageStatus.DELIVERED);
        chatMessage.setDeliveredAt(LocalDateTime.now());

        ChatMessage updatedMessage = chatMessageRepository.save(chatMessage);

        notifyMessageStatusUpdate(updatedMessage);
    }

    private void notifyMessageStatusUpdate(ChatMessage message) {
        messagingTemplate.convertAndSendToUser(
                message.getSenderId(),
                "/queue/status",
                chatMessageMapper.toResponse(message));
    }
}