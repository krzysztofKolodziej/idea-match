package com.example.idea_match.user.listener;

import com.example.idea_match.user.event.OnRegistrationCompleteEvent;
import com.example.idea_match.user.service.email.RegistrationEmailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class RegistrationListener {
    
    private final RegistrationEmailService registrationEmailService;
    
    @EventListener
    @Async
    public void handleRegistrationComplete(OnRegistrationCompleteEvent event) {
        log.info("Processing registration complete event for user: {}", event.username());
        
        registrationEmailService.sendRegistrationEmail(
            event.email(),
            event.username(),
            event.verificationToken()
        );
    }
}