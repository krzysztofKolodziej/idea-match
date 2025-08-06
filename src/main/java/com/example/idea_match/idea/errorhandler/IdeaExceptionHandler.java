package com.example.idea_match.idea.errorhandler;

import com.example.idea_match.idea.exceptions.IdeaAccessDeniedException;
import com.example.idea_match.idea.exceptions.IdeaNotFoundException;
import com.example.idea_match.shared.error.ErrorRespond;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.example.idea_match.idea")
@Slf4j
public class IdeaExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(IdeaNotFoundException.class)
    public ErrorRespond handleIdeaNotFound(IdeaNotFoundException ex) {
        log.warn("Idea not found: {}", ex.getMessage());
        return new ErrorRespond(HttpStatus.NOT_FOUND, "Idea not found");
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(IdeaAccessDeniedException.class)
    public ErrorRespond handleIdeaAccessDenied(IdeaAccessDeniedException ex) {
        log.warn("Idea access denied: {}", ex.getMessage());
        return new ErrorRespond(HttpStatus.FORBIDDEN, "Access denied: You can only modify your own ideas");
    }
}
