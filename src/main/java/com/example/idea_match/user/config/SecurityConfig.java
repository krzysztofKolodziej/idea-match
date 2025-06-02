package com.example.idea_match.user.config;

import com.example.idea_match.user.jwt.JwtAuthorizationFilter;
import com.example.idea_match.user.jwt.JwtUtils;
import com.example.idea_match.user.jwt.RedisTokenBlacklistService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Bean
    SecurityFilterChain filterChain(HttpSecurity http,
                                    AuthenticationManager authenticationManager,
                                    CustomUserDetailsService userDetailsService,
                                    RedisTokenBlacklistService tokenBlacklistService,
                                    JwtUtils jwtUtils) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/account/**").authenticated()
                        .anyRequest().permitAll());

        http.addFilterBefore(
                new JwtAuthorizationFilter(authenticationManager, userDetailsService, tokenBlacklistService, jwtUtils),
                UsernamePasswordAuthenticationFilter.class);

        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.formLogin(AbstractHttpConfigurer::disable);

        http.cors(Customizer.withDefaults());

        return http.build();
    }

}
