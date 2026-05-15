package com.glinscravings;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository - all database operations for Users.
 * Spring Data JPA auto-implements these method signatures.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find a user by username (used for login lookup)
    Optional<User> findByUsername(String username);

    // Check if a username is already taken (used in registration)
    boolean existsByUsername(String username);
}