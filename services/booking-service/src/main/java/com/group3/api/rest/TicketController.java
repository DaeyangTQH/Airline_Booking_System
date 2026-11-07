package com.group3.api.rest;

import com.group3.application.port.in.GetTicketUseCase;
import com.group3.application.query.TicketDetailView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final GetTicketUseCase getTicketUseCase;

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
}
