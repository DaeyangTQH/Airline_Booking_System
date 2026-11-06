package com.group3.application.port.in;

import com.group3.application.command.AddPassengerCommand;
import com.group3.application.result.AddPassengerResult;

public interface AddPassengerUseCase {

    AddPassengerResult addPassenger(
            AddPassengerCommand command
    );
}
