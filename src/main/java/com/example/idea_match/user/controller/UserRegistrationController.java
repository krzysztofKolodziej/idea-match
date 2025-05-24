package com.example.idea_match.user.controller;

import com.example.idea_match.user.command.AddUserCommand;
import com.example.idea_match.user.service.UserAccountFacade;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class UserRegistrationController {

    private UserAccountFacade userAccount;

    @PostMapping("/registration")
    public ResponseEntity<String> userRegistration(AddUserCommand addUserCommand) {
        userAccount.userRegistration(addUserCommand);
        return ResponseEntity.status(HttpStatus.CREATED).body("User successfully added");
    }
}
