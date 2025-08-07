package com.example.idea_match.config;

import com.example.idea_match.shared.security.TokenBlacklistService;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
public class TestConfig {

    @Bean
    @Primary
    public TokenBlacklistService tokenBlacklistService() {
        TokenBlacklistService mockService = mock(TokenBlacklistService.class);
        when(mockService.isBlacklisted(anyString())).thenReturn(false);
        return mockService;
    }
}