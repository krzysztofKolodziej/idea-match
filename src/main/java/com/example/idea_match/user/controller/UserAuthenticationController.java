package com.example.idea_match.user.controller;


import com.example.idea_match.user.dto.AuthResponse;
import com.example.idea_match.user.dto.ForgotPasswordRequest;
import com.example.idea_match.user.dto.LoginRequest;
import com.example.idea_match.user.dto.ResetPasswordRequest;
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
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        String token = userAuthenticationService.login(loginRequest);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("auth/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        log.info("Password reset request for email: {}", request.getEmail());
        userAuthenticationService.initiatePasswordReset(request.getEmail());
        return ResponseEntity.accepted().build();
    }

    @GetMapping("auth/reset-password")
    public ResponseEntity<Void> validateResetToken(@RequestParam String token) {
        log.info("Validating password reset token");
        userAuthenticationService.validatePasswordResetToken(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("auth/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        log.info("Password reset attempt with token");
        userAuthenticationService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}
