package com.example.idea_match.shared.security.auth;

import lombok.Getter;
import com.example.idea_match.user.model.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@Getter
public class CustomUserDetails extends org.springframework.security.core.userdetails.User {

    private final User user;

    public CustomUserDetails(User user) {
        super(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
        this.user = user;
    }
}
