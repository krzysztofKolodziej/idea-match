package com.example.idea_match.user.listener;

import com.example.idea_match.user.event.OnRegistrationCompleteEvent;
import com.example.idea_match.user.model.User;
import com.example.idea_match.user.service.EmailServiceInterface;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class RegistrationListener {
    
    private final EmailServiceInterface emailService;
    
    @EventListener
    @Async
    public void handleRegistrationComplete(OnRegistrationCompleteEvent event) {
        User user = event.getUser();
        
        log.info("Processing registration complete event for user: {}", user.getUsername());
        
        emailService.sendRegistrationEmail(
            user.getEmail(),
            user.getUsername(),
            user.getVerificationToken()
        );
    }
}