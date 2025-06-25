package com.example.idea_match.user.service.registration;

import com.example.idea_match.user.command.AddUserCommand;
import com.example.idea_match.user.event.OnRegistrationCompleteEvent;
import com.example.idea_match.user.exceptions.UserAlreadyExistsException;
import com.example.idea_match.user.model.User;
import com.example.idea_match.user.repository.UserRepository;
import com.example.idea_match.user.service.mapper.UserMapper;
import org.springframework.context.ApplicationEventPublisher;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Service
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void userRegistration(AddUserCommand addUserCommand) {
        if (userRepository.existsByUsernameOrEmailOrPhoneNumber(
                addUserCommand.username(),
                addUserCommand.email(),
                addUserCommand.phoneNumber()
        )){
            throw new UserAlreadyExistsException("Provided user, email or phone number exist");
        }

        User mappedUser = userMapper.dtoToEntity(addUserCommand);
        User userWithEncoderPassword = setPasswordEncoder(mappedUser);
        User finalUser = createVerificationToken(userWithEncoderPassword, LocalDateTime.now().plusHours(24));

        userRepository.save(finalUser);
        
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(this, finalUser));
    }

    private User setPasswordEncoder(User user) {
        String hashPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashPassword);
        return user;
    }

    private User createVerificationToken(User user, LocalDateTime expirationTime) {
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setTokenExpirationTime(expirationTime);
        return user;
    }
}
