package com.example.idea_match.user.service;

import com.example.idea_match.user.command.AddUserCommand;
import com.example.idea_match.user.model.User;
import com.example.idea_match.user.repository.UserRepository;
import com.example.idea_match.user.service.mapper.UserMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
class UserRegistrationService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    void userRegistration(AddUserCommand addUserCommand) {

        User user = userMapper.dtoToEntity(addUserCommand);
        System.out.println(user.getTokenExpirationTime());
        userRepository.save(user);
    }
}
