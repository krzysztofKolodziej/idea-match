package com.example.idea_match.chat.errorhandler;

import com.example.idea_match.chat.dto.ErrorMessage;
import com.example.idea_match.chat.exceptions.ChatMessageException;
import com.example.idea_match.chat.exceptions.NotFoundMessageException;
import com.example.idea_match.chat.exceptions.UserNotAuthenticatedException;
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

    @MessageExceptionHandler(NotFoundMessageException.class)
    @SendToUser("/queue/errors")
    public ErrorMessage handleNotFoundMessageException(NotFoundMessageException ex) {
        log.warn("Not found message error: {}", ex.getMessage());
        return new ErrorMessage("NOT_FOUND","Not found message. Please try again.");
    }

    @MessageExceptionHandler(UserNotAuthenticatedException.class)
    @SendToUser("/queue/errors")
    public ErrorMessage handleUserNotAuthenticatedException(UserNotAuthenticatedException ex) {
        log.warn("User not authenticated error: {}", ex.getMessage());
        return new ErrorMessage("NOT_AUTHENTICATED", "User not authenticated. Please log in.");
    }
}