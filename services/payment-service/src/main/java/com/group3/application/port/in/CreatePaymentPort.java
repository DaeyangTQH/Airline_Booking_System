package com.group3.application.port.in;

import com.group3.application.command.CreatePaymentCommand;
import com.group3.application.result.CreatePaymentResult;

public interface CreatePaymentPort {
    CreatePaymentResult create(CreatePaymentCommand command);
}
