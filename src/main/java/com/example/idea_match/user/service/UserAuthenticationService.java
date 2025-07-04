package com.example.idea_match.user.service;

import com.example.idea_match.user.config.CustomUserDetails;
import com.example.idea_match.user.dto.LoginRequestDto;
import com.example.idea_match.user.jwt.JwtTokenProvider;
import com.example.idea_match.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class UserAuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public String login(LoginRequestDto loginRequest) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.usernameOrEmail(),
                        loginRequest.password())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authenticate.getPrincipal();
        log.info("User {} logged in successfully", userDetails.getAuthorities());

        return jwtTokenProvider.createAccessToken(userDetails.getUser());
    }



}
