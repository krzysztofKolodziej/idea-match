package com.example.idea_match.user.service;

import com.example.idea_match.user.exceptions.ExpiredVerificationTokenException;
import com.example.idea_match.user.exceptions.InvalidTokenException;
import com.example.idea_match.user.exceptions.InvalidVerificationTokenException;
import com.example.idea_match.user.model.User;
import com.example.idea_match.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TokenService {

    private final UserRepository userRepository;

    public User createVerificationToken(User user, LocalDateTime expirationTime) {
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setTokenExpirationTime(expirationTime);
        return user;
    }

    @Transactional
    public void validateVerificationToken(String token) {
        User user = userRepository.findByVerificationToken(token)
            .orElseThrow(() -> new InvalidVerificationTokenException("Invalid verification token"));
        
        if (user.getTokenExpirationTime().isBefore(LocalDateTime.now())) {
            throw new ExpiredVerificationTokenException("Verification token has expired");
        }
        
        user.setEnabled(true);
        userRepository.save(user);
    }
    
    public void validatePasswordResetToken(String token) {
        User user = userRepository.findByPasswordResetToken(token)
            .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));
        
        if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Reset token has expired");
        }
    }
}
