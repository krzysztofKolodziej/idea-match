package com.example.idea_match.user.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "idea_match_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastname;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    private String profilePictureUrl;

    private String location;

    private String aboutMe;

    @Column(nullable = false)
    private String password;

//    @OneToMany(mappedBy = "owner")
//    private List<?> ownedProjects = new ArrayList<>();
//
//    @ManyToMany(mappedBy = "members")
//    private List<?> joinedProjects = new ArrayList<>();

    private String verificationToken;

    @Column(nullable = false)
    private LocalDateTime tokenExpirationTime;

    @Column(nullable = false)
    private boolean enabled;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
