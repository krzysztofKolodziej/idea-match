package com.example.idea_match.user.service.registration;

import com.example.idea_match.user.model.User;
import com.example.idea_match.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Service
public class HandlerUUIDToken {

    private final UserRepository userRepository;

    public User createVerificationToken(User user, LocalDateTime expirationTime){
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setTokenExpirationTime(expirationTime);
        return user;
    }
}
