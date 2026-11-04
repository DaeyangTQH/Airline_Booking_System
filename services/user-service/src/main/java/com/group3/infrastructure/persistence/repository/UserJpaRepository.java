package com.group3.infrastructure.persistence.repository;

import com.group3.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {
    Optional<UserJpaEntity> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    Page<UserJpaEntity> findByEmailContainingIgnoreCase(String email, Pageable pageable);
}
