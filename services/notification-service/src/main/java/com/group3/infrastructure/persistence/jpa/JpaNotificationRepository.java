package com.group3.infrastructure.persistence.jpa;
import com.group3.infrastructure.persistence.entity.NotificationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface JpaNotificationRepository extends JpaRepository<NotificationJpaEntity, UUID> {}