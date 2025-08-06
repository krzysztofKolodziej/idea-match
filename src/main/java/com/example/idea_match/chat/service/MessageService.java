package com.example.idea_match.chat.service;

import com.example.idea_match.chat.kafka.KafkaSender;
import com.example.idea_match.chat.dto.ChatMessageResponse;
import com.example.idea_match.chat.command.SendMessageCommand;
import com.example.idea_match.chat.exceptions.ChatMessageException;
import com.example.idea_match.chat.mapper.ChatMessageMapper;
import com.example.idea_match.chat.model.ChatMessage;
import com.example.idea_match.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageMapper chatMessageMapper;
    private final KafkaSender kafkaSender;

    public void sendMessage(SendMessageCommand command, String senderId, String senderUsername) {
        ChatMessage message = chatMessageMapper.toEntity(command, senderId, senderUsername);

        Optional.ofNullable(message).orElseThrow(ChatMessageException::new);

        ChatMessage savedMessage = chatMessageRepository.save(message);

        kafkaSender.send("messaging", savedMessage);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getUnreadMessages(String userId) {
        List<ChatMessage> unreadMessages = chatMessageRepository.findUnreadMessages(userId);
        return chatMessageMapper.toResponseList(unreadMessages);
    }
}