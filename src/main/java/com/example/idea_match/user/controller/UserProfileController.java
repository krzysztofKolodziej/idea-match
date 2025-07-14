package com.example.idea_match.user.controller;

import com.example.idea_match.user.dto.UserResponse;
import com.example.idea_match.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/account")
@RestController
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserResponse> getUser() {
        UserResponse userResponse = userProfileService.getUser();

        return ResponseEntity.ok(userResponse);
    }
}
