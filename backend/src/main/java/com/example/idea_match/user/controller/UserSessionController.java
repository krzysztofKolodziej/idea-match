package com.example.idea_match.user.controller;

import com.example.idea_match.user.service.UserSessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RequestMapping("api/auth")
@RestController
public class UserSessionController {

    private final UserSessionService userSessionService;

    @PostMapping("/logout")
    public ResponseEntity<Void> logoutUser(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        userSessionService.logoutUser(authorizationHeader);

        return ResponseEntity.noContent().build();
    }


}
