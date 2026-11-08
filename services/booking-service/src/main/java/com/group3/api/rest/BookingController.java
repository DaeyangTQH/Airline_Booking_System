package com.group3.api.rest;

import com.group3.api.dto.AddPassengerRequest;
import com.group3.api.dto.CreateBookingRequest;
import com.group3.application.command.AddPassengerCommand;
import com.group3.application.command.CancelBookingCommand;
import com.group3.application.command.CreateBookingCommand;
import com.group3.application.command.InitiatePaymentCommand;
import com.group3.application.port.in.AddPassengerUseCase;
import com.group3.application.port.in.CancelBookingUseCase;
import com.group3.application.port.in.CreateBookingUseCase;
import com.group3.application.port.in.GetBookingPassengersUseCase;
import com.group3.application.port.in.GetBookingUseCase;
import com.group3.application.port.in.GetMyBookingsUseCase;
import com.group3.application.port.in.InitiatePaymentUseCase;
import com.group3.application.query.BookingDetailView;
import com.group3.application.query.BookingPassengerView;
import com.group3.application.query.BookingSummaryView;
import com.group3.application.result.AddPassengerResult;
import com.group3.application.result.CreateBookingResult;
import com.group3.application.result.InitiatePaymentResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final GetBookingUseCase getBookingUseCase;
    private final GetMyBookingsUseCase getMyBookingUseCase;
    private final CreateBookingUseCase createBookingUseCase;
    private final AddPassengerUseCase addPassengerUseCase;
    private final GetBookingPassengersUseCase getBookingPassengersUseCase;
    private final InitiatePaymentUseCase initiatePaymentUseCase;
    private final CancelBookingUseCase cancelBookingUseCase;

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDetailView> getById(
            @PathVariable UUID bookingId,
            @RequestHeader("X-User-Id") UUID userId
            ){
        return ResponseEntity.ok(
                getBookingUseCase.getById(bookingId, userId)
        );
    }

    @GetMapping("/ref/{bookingReference}")
    public ResponseEntity<BookingDetailView> getByReference(
            @PathVariable String bookingReference,
            @RequestHeader("X-User-Id") UUID userId
    ){
        return ResponseEntity.ok(
                getBookingUseCase.getByReference(
                        bookingReference,
                        userId
                )
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

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/{bookingId}/passengers")
    public ResponseEntity<AddPassengerResult> addPassenger(
            @PathVariable UUID bookingId,
            @RequestHeader("X-User-Id") UUID userId,

            @Valid
            @RequestBody
            AddPassengerRequest request
    ){

        AddPassengerCommand command =
                new AddPassengerCommand(
                        bookingId,
                        userId,
                        request.passengerProfileId(),
                        request.fullName(),
                        request.passportNumber(),
                        request.dateOfBirth(),
                        request.contact().phone(),
                        request.contact().email()
                );

        AddPassengerResult result =
                addPassengerUseCase
                        .addPassenger(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result);
    }

    @GetMapping("/{bookingId}/passengers")
    public ResponseEntity<List<BookingPassengerView>> getBookingPassengers(
            @PathVariable UUID bookingId,

            @RequestHeader("X-User-Id") UUID userId
    ){
        return ResponseEntity.ok(
                getBookingPassengersUseCase
                        .getPassengers(bookingId, userId)
        );
    }

    @PostMapping("/{bookingId}/payment")
    public ResponseEntity<InitiatePaymentResult> initiatePayment(
            @PathVariable UUID bookingId,
            @RequestHeader("X-User-Id") UUID userId
    ){
        InitiatePaymentCommand command =
                new InitiatePaymentCommand(
                        bookingId,
                        userId
                );
        InitiatePaymentResult result =
                initiatePaymentUseCase.initiate(command);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable UUID bookingId,
            @RequestHeader("X-User-Id") UUID userId
    ){
        CancelBookingCommand command =
                new CancelBookingCommand(
                        bookingId,
                        userId
                );

        cancelBookingUseCase.cancel(command);

        return ResponseEntity.noContent().build();
    }

}
