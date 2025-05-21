package com.example.idea_match.user.service;

import com.example.idea_match.user.command.AddUserCommand;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class UserAccountFacade {

    private final UserRegistrationService userRegistrationService;

    public void userRegistration(AddUserCommand addUserCommand) {
       userRegistrationService.userRegistration(addUserCommand);
    }
}
