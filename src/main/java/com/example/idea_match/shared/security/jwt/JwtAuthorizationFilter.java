package com.example.idea_match.shared.security.jwt;


import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.idea_match.shared.security.auth.CustomUserDetailsService;
import com.example.idea_match.user.exceptions.BlackListedTokenException;
import com.example.idea_match.user.exceptions.InvalidJwtTokenException;
import com.example.idea_match.shared.security.TokenBlacklistService;
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
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUtils jwtUtils;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, CustomUserDetailsService userDetailsService,
                                  TokenBlacklistService tokenBlacklistService, JwtUtils jwtUtils) {
        super(authenticationManager);
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        String token = request.getHeader(TOKEN_HEADER);

        if (token != null && token.startsWith(TOKEN_PREFIX)) {
            try {
                UsernamePasswordAuthenticationToken authentication = getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (BlackListedTokenException | InvalidJwtTokenException ex) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"status\":\"UNAUTHORIZED\",\"message\":\"" + ex.getMessage() + "\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(String token) {
        String jwtToken = token.replace(TOKEN_PREFIX, "");

        if (tokenBlacklistService.isBlacklisted(jwtToken)) {
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
