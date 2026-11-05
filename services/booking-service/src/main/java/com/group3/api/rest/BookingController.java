package com.group3.api.rest;

import com.group3.application.port.in.GetBookingUseCase;
import com.group3.application.port.in.GetMyBookingUseCase;
import com.group3.application.query.BookingDetailView;
import com.group3.application.query.BookingSumaryView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final GetMyBookingUseCase getMyBookingUseCase;

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
    public ResponseEntity<List<BookingSumaryView>> getMyBookings(
            @RequestHeader("X-User-Id") UUID userId
    ){
        return ResponseEntity.ok(
                getMyBookingUseCase.getByUserId(userId)
        );
    }
}
