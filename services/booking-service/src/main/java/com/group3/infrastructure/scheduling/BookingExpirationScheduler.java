package com.group3.infrastructure.scheduling;

import com.group3.application.port.in.ExpireBookingsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingExpirationScheduler {

    private final ExpireBookingsUseCase expireBookingsUseCase;

    @Scheduled(
            fixedDelayString =
                    "${app.booking-expiration.fixed-delay-ms:60000}"
    )
    public void expireBookings() {

        int expiredCount =
                expireBookingsUseCase
                        .expirePendingBookings(
                                Instant.now()
                        );

        if (expiredCount > 0) {
            log.info(
                    "Expired {} pending bookings",
                    expiredCount
            );
        }
    }
}
