package com.example.idea_match.user.service;

import com.example.idea_match.user.config.CustomUserDetails;
import com.example.idea_match.user.dto.LoginRequest;
import com.example.idea_match.user.event.PasswordResetCompletedEvent;
import com.example.idea_match.user.event.PasswordResetRequestedEvent;
import com.example.idea_match.user.exceptions.InvalidTokenException;
import com.example.idea_match.shared.security.jwt.JwtTokenProvider;
import com.example.idea_match.user.model.User;
import com.example.idea_match.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Service
@Slf4j
public class UserAuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TokenService tokenService;

    public String login(LoginRequest loginRequest) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.usernameOrEmail(),
                        loginRequest.password())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authenticate.getPrincipal();
        log.info("User {} logged in successfully", userDetails.getAuthorities());

        return jwtTokenProvider.createAccessToken(userDetails.getUser());
    }

    @Transactional
    public void initiatePasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String resetToken = generateSecureToken();
            
            user.setPasswordResetToken(resetToken);
            user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);
            
            applicationEventPublisher.publishEvent(
                new PasswordResetRequestedEvent(
                    user.getEmail(), 
                    resetToken, 
                    LocalDateTime.now()
                )
            );
            
            log.info("Password reset initiated for user: {}", email);
        } else {
            log.warn("Password reset attempted for non-existent email: {}", email);
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
            .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));
        
        if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Reset token has expired");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
        
        applicationEventPublisher.publishEvent(
            new PasswordResetCompletedEvent(
                user.getEmail(), 
                LocalDateTime.now()
            )
        );
        
        log.info("Password reset completed for user: {}", user.getEmail());
    }
    
    public void validatePasswordResetToken(String token) {
        tokenService.validatePasswordResetToken(token);
    }
    
    private String generateSecureToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
