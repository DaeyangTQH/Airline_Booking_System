package com.group3.application.service;

import com.group3.application.port.in.GetTicketUseCase;
import com.group3.application.port.out.TicketQueryPort;
import com.group3.application.query.TicketDetailView;
import com.group3.domain.exception.TicketNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetTicketService implements GetTicketUseCase {

    private final TicketQueryPort ticketQueryPort;

    @Override
    @Transactional(readOnly = true)
    public TicketDetailView getById(UUID ticketId, UUID userId) {

        return ticketQueryPort
                .findDetailByIdAndUserId(
                        ticketId,
                        userId
                )
                .orElseThrow(() -> new TicketNotFoundException(
                        "Ticket not found with ID: " + ticketId
                ));
    }
}
