package com.example.idea_match.user.controller;


import com.example.idea_match.user.dto.AuthResponse;
import com.example.idea_match.user.command.ForgotPasswordCommand;
import com.example.idea_match.user.command.LoginCommand;
import com.example.idea_match.user.command.ResetPasswordCommand;
import com.example.idea_match.user.service.UserAuthenticationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@AllArgsConstructor
@RestController
@RequestMapping("/api")
@Slf4j
public class UserAuthenticationController {

    private final UserAuthenticationService userAuthenticationService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginCommand loginCommand) {
        String token = userAuthenticationService.login(loginCommand);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("auth/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordCommand command) {
        log.info("Password reset request for email: {}", command.email());
        userAuthenticationService.initiatePasswordReset(command);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("auth/reset-password")
    public ResponseEntity<Void> validateResetToken(@RequestParam String token) {
        log.info("Validating password reset token");
        userAuthenticationService.validatePasswordResetToken(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("auth/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordCommand command) {
        log.info("Password reset attempt with token");
        userAuthenticationService.resetPassword(command);
        return ResponseEntity.ok().build();
    }
}
