package com.group3.api.rest;

import com.group3.api.dto.AssignTicketSeatRequest;
import com.group3.application.command.AssignSeatCommand;
import com.group3.application.command.RemoveSeatCommand;
import com.group3.application.port.in.AssignSeatUseCase;
import com.group3.application.port.in.GetTicketUseCase;
import com.group3.application.port.in.RemoveSeatUseCase;
import com.group3.application.query.TicketDetailView;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final GetTicketUseCase getTicketUseCase;
    private final AssignSeatUseCase assignSeatUseCase;
    private final RemoveSeatUseCase removeSeatUseCase;

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketDetailView> getById(
            @PathVariable UUID ticketId,

            @RequestHeader("X-User-Id") UUID userId
    ) {
        return ResponseEntity.ok(
                getTicketUseCase.getById(
                        ticketId,
                        userId
                )
        );
    }

    @PutMapping("/{ticketId}/seat")
    public ResponseEntity<Void> assignSeat(
            @PathVariable UUID ticketId,
            @RequestHeader("X-User-Id") UUID userId,

            @Valid
            @RequestBody
            AssignTicketSeatRequest request
    ) {
        AssignSeatCommand command =
                new AssignSeatCommand(
                        ticketId,
                        userId,
                        request.seatId()
                );

        assignSeatUseCase.assignSeat(
                command
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    @DeleteMapping("/{ticketId}/seat")
    public ResponseEntity<Void> removeSeat(
            @PathVariable UUID ticketId,
            @RequestHeader("X-User-Id") UUID userId
    ) {
        RemoveSeatCommand command =
                new RemoveSeatCommand(
                        ticketId,
                        userId
                );

        removeSeatUseCase.removeSeat(
                command
        );

        return ResponseEntity
                .noContent()
                .build();
    }

}
