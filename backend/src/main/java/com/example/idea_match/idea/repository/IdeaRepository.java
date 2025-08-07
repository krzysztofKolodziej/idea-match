package com.example.idea_match.idea.repository;


import com.example.idea_match.idea.model.Idea;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdeaRepository extends JpaRepository<Idea, Long> {

    @EntityGraph(attributePaths = {"owner"})
    Page<Idea> findAll(Specification<Idea> and, Pageable pageable);

    @EntityGraph(attributePaths = {"owner"})
    Optional<Idea> findById(Long id);
}
