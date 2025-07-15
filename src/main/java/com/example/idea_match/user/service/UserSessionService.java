package com.example.idea_match.user.service;

import com.example.idea_match.user.exceptions.InvalidAuthorizationTokenException;
import com.example.idea_match.user.jwt.JwtUtils;
import com.example.idea_match.user.jwt.RedisTokenBlacklistService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@AllArgsConstructor
@Slf4j
@Service
public class UserSessionService {

    private final RedisTokenBlacklistService blacklistService;
    private final JwtUtils jwtUtils;


    public void logoutUser(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new InvalidAuthorizationTokenException("Authorization header is missing or invalid format");
        }

        String token = authorizationHeader.substring(7);

        Date expiration = jwtUtils.getExpiration(token);
        long ttl = Math.max(0, expiration.getTime() - System.currentTimeMillis());

        blacklistService.blacklistToken(token, ttl);
        log.info("Token blacklisted with TTL: {}ms", ttl);
    }
}
