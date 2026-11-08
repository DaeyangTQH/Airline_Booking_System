package com.group3.application.port.in;

import com.group3.application.command.AssignSeatCommand;

public interface AssignSeatUseCase {

    void assignSeat(
            AssignSeatCommand command
    );
}
