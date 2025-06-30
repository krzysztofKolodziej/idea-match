package com.example.idea_match.user.service;

import com.example.idea_match.user.model.User;
import com.example.idea_match.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class HandlerVerificationToken {

    private final UserRepository userRepository;

    public User createVerificationToken(User user, LocalDateTime expirationTime) {
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setTokenExpirationTime(expirationTime);
        return user;
    }

    public String validateVerificationToken(String token) {
        User user = userRepository.findByVerificationToken(token).orElse(null);
        if (user == null) {
            return "invalid";
        }
        else if (user.getTokenExpirationTime().isBefore(java.time.LocalDateTime.now())) {
            return "expired";
        } else {
            user.setEnabled(true);
            userRepository.save(user);
            return "valid";
        }
    }
}
