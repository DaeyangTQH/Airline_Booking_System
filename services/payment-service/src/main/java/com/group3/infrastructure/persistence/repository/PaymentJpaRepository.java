package com.group3.infrastructure.persistence.repository;

import com.group3.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, UUID> {
    Optional<PaymentEntity> findByIdempotencyKey(String idempotencyKey);

    @Query(value = """
            SELECT * FROM payments
            WHERE UPPER(REPLACE(CAST(id AS varchar), '-', '')) LIKE UPPER(CONCAT(:hexPrefix, '%'))
            ORDER BY created_at DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<PaymentEntity> findFirstByIdHexPrefix(@Param("hexPrefix") String hexPrefix);
}
