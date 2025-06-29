package com.example.idea_match.user.event;

import com.example.idea_match.user.model.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OnRegistrationCompleteEvent extends ApplicationEvent {
    
    private final User user;
    
    public OnRegistrationCompleteEvent(Object source, User user) {
        super(source);
        this.user = user;
    }
}