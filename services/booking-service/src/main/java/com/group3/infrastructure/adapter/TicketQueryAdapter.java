package com.group3.infrastructure.adapter;

import com.group3.application.port.out.TicketQueryPort;
import com.group3.application.query.TicketDetailView;
import com.group3.infrastructure.persistence.TicketJpaRepository;
import com.group3.infrastructure.persistence.entity.TicketEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TicketQueryAdapter implements TicketQueryPort {

    private final TicketJpaRepository ticketJpaRepository;

    @Override
    public Optional<TicketDetailView> findDetailByIdAndUserId(UUID ticketId, UUID userId) {

        return ticketJpaRepository
                .findDetailByIdAndUserId(ticketId, userId)
                .map(this::toDetailView);
    }

    private TicketDetailView toDetailView(TicketEntity entity){

        return new TicketDetailView(
                entity.getId(),
                entity.getTicketNumber(),
                entity.getPassenger()
                        .getBooking()
                        .getId(),

                entity.getPassenger().getId(),

                entity.getSeatId(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
