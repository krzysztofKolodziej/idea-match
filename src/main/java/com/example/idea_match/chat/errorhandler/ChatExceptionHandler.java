package com.example.idea_match.chat.errorhandler;

import com.example.idea_match.chat.dto.ErrorMessage;
import com.example.idea_match.chat.exceptions.ChatMessageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
@Slf4j
public class ChatExceptionHandler {

    @MessageExceptionHandler(ChatMessageException.class)
    @SendToUser("/queue/errors")
    public ErrorMessage handleChatMessageException(ChatMessageException ex) {
        log.warn("Chat message error: {}", ex.getMessage());
        return new ErrorMessage("INVALID_MESSAGE","Failed to process message. Please try again.");
    }
}