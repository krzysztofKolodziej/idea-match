package com.example.idea_match.user.service;

import com.example.idea_match.user.command.RegisterUserCommand;
import com.example.idea_match.user.event.OnRegistrationCompleteEvent;
import com.example.idea_match.user.exceptions.UserAlreadyExistsException;
import com.example.idea_match.user.model.User;
import com.example.idea_match.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@AllArgsConstructor
@Service
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final TokenService tokenService;

    @Transactional
    public void userRegistration(RegisterUserCommand registerUserCommand) {
        if (userRepository.existsByUsernameOrEmailOrPhoneNumber(
                registerUserCommand.username(),
                registerUserCommand.email(),
                registerUserCommand.phoneNumber()
        )){
            throw new UserAlreadyExistsException("Provided user, email or phone number exist");
        }

        User mappedUser = userMapper.commandToEntity(registerUserCommand);
        User userWithEncoderPassword = setPasswordEncoder(mappedUser);
        User finalUser = tokenService
                .createVerificationToken(userWithEncoderPassword, LocalDateTime.now().plusHours(24));

        userRepository.save(finalUser);
        
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(
            finalUser.getEmail(),
            finalUser.getUsername(),
            finalUser.getVerificationToken(),
            LocalDateTime.now()
        ));
    }

    private User setPasswordEncoder(User user) {
        String hashPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashPassword);
        return user;
    }

}
