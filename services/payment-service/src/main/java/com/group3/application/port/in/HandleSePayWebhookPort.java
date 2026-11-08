package com.group3.application.port.in;

import com.group3.application.command.HandleSePayWebhookCommand;

public interface HandleSePayWebhookPort {

    /**
     * @return true nếu webhook được chấp nhận (SePay cần {@code {"success":true}})
     */
    boolean handle(HandleSePayWebhookCommand command);
}
