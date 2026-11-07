package com.group3.application.port.out;

import com.group3.application.query.TicketDetailView;

import java.util.Optional;
import java.util.UUID;

public interface TicketQueryPort {

    Optional<TicketDetailView> findDetailByIdAndUserId(
            UUID ticketId,
            UUID userId
    );
}
