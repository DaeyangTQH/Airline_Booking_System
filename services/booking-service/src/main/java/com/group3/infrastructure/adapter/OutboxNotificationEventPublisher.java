package com.group3.infrastructure.adapter;

import com.group3.application.integration.NotificationEvent;
import com.group3.application.port.out.NotificationEventPublisher;
import com.group3.infrastructure.persistence.OutboxEventJpaRepository;
import com.group3.infrastructure.persistence.entity.OutboxEventEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class OutboxNotificationEventPublisher
        implements NotificationEventPublisher {

    private final OutboxEventJpaRepository outboxRepository;
    private final JsonMapper jsonMapper;

    @Override
    public void publish(
            NotificationEvent event
    ) {
        if (event == null) {
            throw new IllegalArgumentException(
                    "Notification event must not be null"
            );
        }

        try {
            String payload =
                    jsonMapper.writeValueAsString(event);

            OutboxEventEntity outboxEvent =
                    OutboxEventEntity.pending(
                            event.eventId(),
                            event.type().name(),
                            payload
                    );

            outboxRepository.save(outboxEvent);

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Cannot serialize notification event",
                    exception
            );
        }
    }
}
