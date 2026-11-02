
package com.group3.repository;

import com.group3.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Find user by email
    Optional<User> findByEmailIgnoreCase(String email);

    // Check whether email already exists
    boolean existsByEmailIgnoreCase(String email);

    // Search users by email with pagination
    Page<User> findByEmailContainingIgnoreCase(
            String email,
            Pageable pageable
    );
}
