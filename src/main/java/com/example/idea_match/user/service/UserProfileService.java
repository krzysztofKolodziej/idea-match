package com.example.idea_match.user.service;

import com.example.idea_match.user.command.UpdateUserProfileCommand;
import com.example.idea_match.user.dto.UserResponse;
import com.example.idea_match.user.exceptions.PhoneNumberAlreadyExistsException;
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
        User user = getCurrentUser();
        log.debug("Successfully retrieved user profile for ID: {}", user.getId());
        return userMapper.entityToDto(user);
    }

    @Transactional
    public UserResponse updateUserProfile(UpdateUserProfileCommand command) {
        User user = getCurrentUser();
        
        if (!user.getPhoneNumber().equals(command.phoneNumber()) &&
            userRepository.existsByPhoneNumber(command.phoneNumber())) {
            throw new PhoneNumberAlreadyExistsException("Phone number already exists: " + command.phoneNumber());
        }
        
        userMapper.updateUserFromCommand(command, user);
        User savedUser = userRepository.save(user);
        log.debug("Successfully updated user profile for ID: {}", savedUser.getId());
        return userMapper.entityToDto(savedUser);
    }

    @Transactional
    public void deleteCurrentUser() {
        User user = getCurrentUser();
        userRepository.delete(user);
        log.info("User deleted successfully: {} (ID: {})", user.getUsername(), user.getId());
    }

    private User getCurrentUser() {
        String usernameOrEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        
        return userRepository.findByUsernameOrEmail(
                        usernameOrEmail.contains("@") ? null : usernameOrEmail,
                        usernameOrEmail.contains("@") ? usernameOrEmail : null)
                .orElseThrow(() -> new UsernameOrEmailNotFoundException("User not found: " + usernameOrEmail));
    }
}
