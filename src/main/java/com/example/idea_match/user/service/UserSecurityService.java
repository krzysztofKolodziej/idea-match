package com.example.idea_match.user.service;

import com.example.idea_match.user.command.ChangePasswordCommand;
import com.example.idea_match.user.exceptions.IncorrectUserPasswordException;
import com.example.idea_match.user.exceptions.UsernameOrEmailNotFoundException;
import com.example.idea_match.user.model.User;
import com.example.idea_match.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserSecurityService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void changePassword(ChangePasswordCommand request) {
        String usernameOrEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsernameOrEmail(
                        usernameOrEmail.contains("@") ? null : usernameOrEmail,
                        usernameOrEmail.contains("@") ? usernameOrEmail : null)
                .orElseThrow(() -> new UsernameOrEmailNotFoundException("User not found: " + usernameOrEmail));

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new IncorrectUserPasswordException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user {}", user.getUsername());
    }
}
