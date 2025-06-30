package com.example.idea_match.user.controller;

import com.example.idea_match.user.command.AddUserCommand;
import com.example.idea_match.user.service.HandlerVerificationToken;
import com.example.idea_match.user.service.registration.UserRegistrationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@AllArgsConstructor
@RestController
public class UserRegistrationController {

    private UserRegistrationService userRegistration;
    private HandlerVerificationToken handlerVerificationToken;

    @PostMapping("/registration")
    public ResponseEntity<String> userRegistration(@RequestBody @Valid AddUserCommand addUserCommand) {
        userRegistration.userRegistration(addUserCommand);
        return ResponseEntity.status(HttpStatus.CREATED).body("User successfully added");
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmailRegistration(@RequestParam String token) {
        String result = handlerVerificationToken.validateVerificationToken(token);
        if (result.equals("valid")) {
            return ResponseEntity.status(HttpStatus.FOUND).body("Your account has been verified successfully.");
        } else if (result.equals("expired")) {
            return ResponseEntity.status(HttpStatus.GONE).body("Verification token has been expired.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid verification token.");
        }
    }
}
