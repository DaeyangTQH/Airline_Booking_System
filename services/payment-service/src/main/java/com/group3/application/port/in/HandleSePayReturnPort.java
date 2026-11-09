package com.group3.application.port.in;

import com.group3.application.command.HandleSePayReturnCommand;

public interface HandleSePayReturnPort {

    /**
     * @return true nếu payment đã/được complete thành công
     */
    boolean handleSuccess(HandleSePayReturnCommand command);
}
