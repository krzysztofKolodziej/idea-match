package com.example.idea_match.user.controller;


import com.example.idea_match.user.dto.LoginRequestDto;
import com.example.idea_match.user.service.UserAuthenticationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class UserAuthenticationController {

    private final UserAuthenticationService userAuthenticationService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid LoginRequestDto loginRequest) {
        String token = userAuthenticationService.login(loginRequest);
        return ResponseEntity.ok(token);
    }
}
