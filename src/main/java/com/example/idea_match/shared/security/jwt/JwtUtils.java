package com.example.idea_match.shared.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;


@Component
public class JwtUtils {

    private final String secret;
    private final long expirationTime;

    public JwtUtils(@Value("${jwt.secret}") String secret,
                    @Value("${jwt.expirationTime}") long expirationTime) {
        this.secret = secret;
        this.expirationTime = expirationTime;
    }

    public String generateToken(String username, String role) {
        return JWT.create()
                .withSubject(username)
                .withClaim("role", role)
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationTime))
                .sign(Algorithm.HMAC256(secret));
    }

    public String getUsername(String token) {
        return JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token)
                .getSubject();
    }

    public String getRole(String token) {
        return JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token)
                .getClaim("role")
                .asString();
    }

    public Date getExpiration(String token) {
        return JWT.require(Algorithm.HMAC256(secret))
                .build().verify(token)
                .getExpiresAt();
    }

    public String validateTokenAndGetUsername(String token) throws JWTVerificationException {
        return JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token)
                .getSubject();
    }
}
