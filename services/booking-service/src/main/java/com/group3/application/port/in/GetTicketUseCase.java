package com.group3.application.port.in;

import com.group3.application.query.TicketDetailView;

import java.util.UUID;

public interface GetTicketUseCase {

    TicketDetailView getById(
            UUID ticketId,
            UUID userId
    );
}
