package com.group3.api.rest;

import com.group3.api.dto.CreateBookingRequest;
import com.group3.application.command.CreateBookingCommand;
import com.group3.application.port.in.CreateBookingUseCase;
import com.group3.application.port.in.GetBookingUseCase;
import com.group3.application.port.in.GetMyBookingsUseCase;
import com.group3.application.query.BookingDetailView;
import com.group3.application.query.BookingSummaryView;
import com.group3.application.result.CreateBookingResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final GetBookingUseCase getBookingUseCase;
    private final GetMyBookingsUseCase getMyBookingUseCase;
    private final CreateBookingUseCase createBookingUseCase;

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDetailView> getById(
            @PathVariable UUID bookingId
            ){
        return ResponseEntity.ok(
                getBookingUseCase.getById(bookingId)
        );
    }

    @GetMapping("/ref/{bookingReference}")
    public ResponseEntity<BookingDetailView> getByReference(
            @PathVariable String bookingReference
    ){
        return ResponseEntity.ok(
                getBookingUseCase.getByReference(bookingReference)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<List<BookingSummaryView>> getMyBookings(
            @RequestHeader("X-User-Id") UUID userId
    ){
        return ResponseEntity.ok(
                getMyBookingUseCase.getByUserId(userId)
        );
    }

    @PostMapping
    public ResponseEntity<CreateBookingResult> createBooking(
            @RequestHeader("X-User-Id") UUID userId,

            @Valid
            @RequestBody
            CreateBookingRequest request
    ){
        CreateBookingCommand command =
                new CreateBookingCommand(
                        userId,
                        request.flightId(),
                        request.fareClass(),
                        request.quantity(),
                        request.contact().phone(),
                        request.contact().email()
                );

        CreateBookingResult result =
                createBookingUseCase.create(command);

        URI location =
                ServletUriComponentsBuilder
                        .fromCurrentRequest()
                        .path("/{bookingId}")
                        .buildAndExpand(result.bookingId())
                        .toUri();

        return ResponseEntity.created(location).body(result);
    }
}
