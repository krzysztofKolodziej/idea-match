package com.example.idea_match.user.jwt;


import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.idea_match.user.config.CustomUserDetailsService;
import com.example.idea_match.user.exceptions.BlackListedTokenException;
import com.example.idea_match.user.exceptions.InvalidJwtTokenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;


public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private final CustomUserDetailsService userDetailsService;
    private final RedisTokenBlacklistService redisTokenBlacklistService;
    private final JwtUtils jwtUtils;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, CustomUserDetailsService userDetailsService,
                                  RedisTokenBlacklistService redisTokenBlacklistService, JwtUtils jwtUtils) {
        super(authenticationManager);
        this.userDetailsService = userDetailsService;
        this.redisTokenBlacklistService = redisTokenBlacklistService;
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        String token = request.getHeader(TOKEN_HEADER);

        if (token != null && token.startsWith(TOKEN_PREFIX)) {
            UsernamePasswordAuthenticationToken authentication = getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(String token) {
        String jwtToken = token.replace(TOKEN_PREFIX, "");

        if (redisTokenBlacklistService.isTokenBlacklisted(jwtToken)) {
            throw new BlackListedTokenException("Token is blacklisted");
        }

        try {
            String username = jwtUtils.validateTokenAndGetUsername(jwtToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        } catch (JWTVerificationException ex) {
            throw new InvalidJwtTokenException("Invalid JWT token: " + ex);
        }
    }
}
