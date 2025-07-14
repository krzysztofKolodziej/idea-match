package com.example.idea_match.user.controller;

import com.example.idea_match.user.command.ChangePasswordCommand;
import com.example.idea_match.user.service.UserSecurityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("api/account")
@RestController
public class UserSecurityController {

    private final UserSecurityService userSecurityService;

    @PostMapping("/change-password")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordCommand request) {
        userSecurityService.changePassword(request);

        return ResponseEntity.ok().build();
    }
}
