package com.group3.application.port.in;

import java.time.Instant;

public interface ExpireBookingsUseCase {

    int expirePendingBookings(Instant now);
}
