package com.example.idea_match.user.repository;

import com.example.idea_match.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsernameOrEmailOrPhoneNumber(String username, String email, String phoneNumber);

    Optional<User> findByVerificationToken(String token);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> deleteByUsername(String username);
}
