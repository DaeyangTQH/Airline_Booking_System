package com.group3.application.port.in;

import com.group3.application.command.RemoveSeatCommand;

public interface RemoveSeatUseCase {

    void removeSeat(
            RemoveSeatCommand command
    );
}

