package com.example.idea_match.chat.websocket;

import com.example.idea_match.chat.exceptions.WebSocketTokenBlacklistedException;
import com.example.idea_match.chat.exceptions.WebSocketTokenInvalidException;
import com.example.idea_match.chat.exceptions.WebSocketTokenMissingException;
import com.example.idea_match.shared.security.TokenBlacklistService;
import com.example.idea_match.shared.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public Message<?> preSend(@NonNull Message<?> message,@NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && isConnectCommand(accessor)) {
            return authenticateConnection(accessor, message);
        }
        
        return message;
    }

    private boolean isConnectCommand(StompHeaderAccessor accessor) {
        return StompCommand.CONNECT.equals(accessor.getCommand());
    }

    private Message<?> authenticateConnection(StompHeaderAccessor accessor, Message<?> originalMessage) {
        Optional<String> tokenOpt = extractBearerToken(accessor);

        if (tokenOpt.isEmpty()) {
            throw new WebSocketTokenMissingException();
        }
        
        String token = tokenOpt.get();
        if (tokenBlacklistService.isBlacklisted(token)) {
            throw new WebSocketTokenBlacklistedException();
        }
        
        return authenticateUser(token, accessor, originalMessage);
    }

    private Optional<String> extractBearerToken(StompHeaderAccessor accessor) {
        return Optional.ofNullable(accessor.getFirstNativeHeader("Authorization"))
                .filter(StringUtils::hasText)
                .filter(header -> header.startsWith("Bearer "))
                .map(header -> header.substring(7).trim())
                .filter(StringUtils::hasText);
    }


    private Message<?> authenticateUser(String token, StompHeaderAccessor accessor, Message<?> originalMessage) {
            String username = jwtUtils.validateTokenAndGetUsername(token);
            String role = jwtUtils.getRole(token);
            
            if (!StringUtils.hasText(username)) {
                throw new WebSocketTokenInvalidException();
            }
            
            setSessionAttributes(accessor, username, role);
            log.info("WebSocket connection authenticated for user: {}", username);
            
            return originalMessage;
    }

    private void setSessionAttributes(StompHeaderAccessor accessor, String username, String role) {
        String normalizedRole = StringUtils.hasText(role) ? role.toUpperCase() : "USER";
        
        UsernamePasswordAuthenticationToken authToken = 
            new UsernamePasswordAuthenticationToken(
                username, 
                null, 
                List.of(new SimpleGrantedAuthority("ROLE_" + normalizedRole))
            );

        Objects.requireNonNull(accessor.getSessionAttributes()).put("username", username);
        accessor.getSessionAttributes().put("userId", username);
        accessor.getSessionAttributes().put("role", normalizedRole);
        accessor.getSessionAttributes().put("authenticated", true);
        accessor.setUser(authToken);
    }

}