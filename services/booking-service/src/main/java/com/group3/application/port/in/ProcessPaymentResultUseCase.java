package com.group3.application.port.in;

import com.group3.application.command.ProcessPaymentResultCommand;

public interface ProcessPaymentResultUseCase {

    void process(
            ProcessPaymentResultCommand command
    );
}
