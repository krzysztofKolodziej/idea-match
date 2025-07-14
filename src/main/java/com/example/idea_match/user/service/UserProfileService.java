package com.example.idea_match.user.service;

import com.example.idea_match.user.dto.UserResponse;
import com.example.idea_match.user.exceptions.UsernameOrEmailNotFoundException;
import com.example.idea_match.user.model.User;
import com.example.idea_match.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponse getUser() {
        String usernameOrEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsernameOrEmail(
                        usernameOrEmail.contains("@") ? null : usernameOrEmail,
                        usernameOrEmail.contains("@") ? usernameOrEmail : null)
                .orElseThrow(() -> new UsernameOrEmailNotFoundException("User not found: " + usernameOrEmail));

        log.debug("Successfully retrieved user profile for ID: {}", user.getId());

        return userMapper.entityToDto(user);
    }

    @Transactional
    public void deleteCurrentUser() {
        String usernameOrEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        
        User user = userRepository.findByUsernameOrEmail(
                        usernameOrEmail.contains("@") ? null : usernameOrEmail,
                        usernameOrEmail.contains("@") ? usernameOrEmail : null)
                .orElseThrow(() -> new UsernameOrEmailNotFoundException("User not found: " + usernameOrEmail));
        
        userRepository.delete(user);
        
        log.info("User deleted successfully: {} (ID: {})", user.getUsername(), user.getId());
    }
}
