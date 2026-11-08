package com.group3.application.port.out;

import com.group3.application.integration.NotificationEvent;

public interface NotificationEventPublisher {

    void publish(NotificationEvent event);
}

