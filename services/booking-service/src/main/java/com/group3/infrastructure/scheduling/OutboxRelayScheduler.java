package com.group3.infrastructure.scheduling;

import com.group3.infrastructure.messaging.RabbitNotificationEventPublisher;
import com.group3.infrastructure.persistence.OutboxEventJpaRepository;
import com.group3.infrastructure.persistence.entity.OutboxEventEntity;
import com.group3.infrastructure.persistence.entity.OutboxStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxRelayScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(
                    OutboxRelayScheduler.class
            );

    private final OutboxEventJpaRepository
            outboxRepository;

    private final RabbitNotificationEventPublisher
            rabbitPublisher;

    private final int maxRetries;

    public OutboxRelayScheduler(
            OutboxEventJpaRepository outboxRepository,

            RabbitNotificationEventPublisher rabbitPublisher,

            @Value("${app.outbox.max-retries:5}")
            int maxRetries
    ) {
        this.outboxRepository =
                outboxRepository;

        this.rabbitPublisher =
                rabbitPublisher;

        this.maxRetries =
                maxRetries;
    }

    @Scheduled(
            fixedDelayString =
                    "${app.outbox.fixed-delay-ms:5000}"
    )
    @Transactional
    public void publishPendingEvents() {

        List<OutboxEventEntity> pendingEvents =
                outboxRepository
                        .findTop50ByStatusOrderByCreatedAtAsc(
                                OutboxStatus.PENDING
                        );

        for (OutboxEventEntity event
                : pendingEvents) {

            publishEvent(event);
        }
    }

    private void publishEvent(
            OutboxEventEntity event
    ) {
        try {
            rabbitPublisher.publish(
                    event.getPayload()
            );

            event.markPublished();

            log.info(
                    "Published outbox event: id={}, type={}",
                    event.getId(),
                    event.getEventType()
            );

        } catch (Exception exception) {

            event.registerFailure(
                    exception.getMessage(),
                    maxRetries
            );

            log.error(
                    "Failed to publish outbox event: "
                            + "id={}, type={}, retry={}, status={}",
                    event.getId(),
                    event.getEventType(),
                    event.getRetryCount(),
                    event.getStatus(),
                    exception
            );
        }

        outboxRepository.save(event);
    }
}
