package com.example.idea_match.user.controller;

import com.example.idea_match.user.command.AddUserCommand;
import com.example.idea_match.user.service.TokenService;
import com.example.idea_match.user.service.UserRegistrationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@AllArgsConstructor
@RequestMapping("/api")
@RestController
public class UserRegistrationController {

    private UserRegistrationService userRegistration;
    private TokenService tokenService;

    @PostMapping("/registration")
    public ResponseEntity<Void> userRegistration(@RequestBody @Valid AddUserCommand addUserCommand) {
        userRegistration.userRegistration(addUserCommand);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Void> verifyEmailRegistration(@RequestParam String token) {
        tokenService.validateVerificationToken(token);
        return ResponseEntity.ok().build();
    }
}
