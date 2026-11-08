package com.group3.infrastructure.persistence;

import com.group3.infrastructure.persistence.entity.OutboxEventEntity;
import com.group3.infrastructure.persistence.entity.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventJpaRepository
        extends JpaRepository<OutboxEventEntity, UUID> {

    List<OutboxEventEntity>
    findTop50ByStatusOrderByCreatedAtAsc(
            OutboxStatus status
    );
}
