package com.group3.application.port.in;

import com.group3.application.command.HandleSePayReturnCommand;

public interface HandleSePayReturnPort {

    void handleSuccess(HandleSePayReturnCommand command);
}
