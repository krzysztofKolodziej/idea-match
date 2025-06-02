package com.example.idea_match.user.jwt;

import com.example.idea_match.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class JwtTokenProvider {

    private final JwtUtils jwtUtils;

    public String createAccessToken(User user) {
        return jwtUtils.generateToken(user.getUsername(), user.getRole().name());
    }
}
