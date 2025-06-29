package com.example.idea_match.user.controller;

import com.example.idea_match.user.command.AddUserCommand;
import com.example.idea_match.user.service.registration.UserRegistrationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@AllArgsConstructor
@RestController
public class UserRegistrationController {

    private UserRegistrationService userRegistration;

    @PostMapping("/registration")
    public ResponseEntity<String> userRegistration(@RequestBody @Valid AddUserCommand addUserCommand) {
        userRegistration.userRegistration(addUserCommand);
        return ResponseEntity.status(HttpStatus.CREATED).body("User successfully added");
    }
}
