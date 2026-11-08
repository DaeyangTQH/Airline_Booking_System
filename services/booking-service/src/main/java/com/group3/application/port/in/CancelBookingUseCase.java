package com.group3.application.port.in;

import com.group3.application.command.CancelBookingCommand;

public interface CancelBookingUseCase {

    void cancel(
            CancelBookingCommand command
    );
}
