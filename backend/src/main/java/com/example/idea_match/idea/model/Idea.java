package com.example.idea_match.idea.model;

import com.example.idea_match.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "idea")
public class Idea {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 200)
    private String location;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(length = 1000)
    private String goal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdeaStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdeaCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToMany(mappedBy = "joinedProjects")
    @Builder.Default
    private List<User> collaborators = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    private LocalDateTime expectedStartDate;
}
