package com.group3.infrastructure.messaging;

import com.group3.application.port.out.UserEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class RabbitUserEventPublisher implements UserEventPublisherPort {

    private static final Logger log =
            LoggerFactory.getLogger(RabbitUserEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitUserEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishProfileUpdated(String email) {

        Runnable sendMessage = () -> {
            String message = email
                    + "|USER_PROFILE_UPDATED|"
                    + "Your profile has been updated successfully.";

            try {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.NOTIFICATION_QUEUE,
                        message
                );

                log.info("Published USER_PROFILE_UPDATED event");

            } catch (AmqpException ex) {
                log.error("Cannot publish profile update event", ex);
            }
        };

        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {

            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            sendMessage.run();
                        }
                    }
            );

        } else {
            sendMessage.run();
        }
    }
}