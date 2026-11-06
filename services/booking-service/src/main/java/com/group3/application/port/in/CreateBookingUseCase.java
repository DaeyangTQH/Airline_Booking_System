package com.group3.application.port.in;

import com.group3.application.command.CreateBookingCommand;
import com.group3.application.result.CreateBookingResult;

public interface CreateBookingUseCase {

    CreateBookingResult create(
            CreateBookingCommand command
    );
}
