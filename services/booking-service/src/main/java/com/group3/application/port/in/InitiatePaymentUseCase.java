package com.group3.application.port.in;

import com.group3.application.command.InitiatePaymentCommand;
import com.group3.application.result.InitiatePaymentResult;

public interface InitiatePaymentUseCase {

    InitiatePaymentResult initiate(
            InitiatePaymentCommand command
    );
}
