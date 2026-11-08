package com.group3.api.rest;

import com.group3.api.dto.SePayWebhookRequest;
import com.group3.api.dto.SePayWebhookResponse;
import com.group3.application.command.HandleSePayWebhookCommand;
import com.group3.application.port.in.HandleSePayWebhookPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SePayWebhookController {

    HandleSePayWebhookPort handleSePayWebhookPort;

    @PostMapping({
            "/api/v1/payments/sepay/webhook",
            "/api/sepay/webhook"
    })
    public ResponseEntity<SePayWebhookResponse> webhook(
            @RequestBody SePayWebhookRequest body,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        boolean ok = handleSePayWebhookPort.handle(
                new HandleSePayWebhookCommand(
                        body == null ? null : body.id(),
                        body == null ? null : body.code(),
                        body == null ? null : body.content(),
                        body == null ? null : body.transferType(),
                        body == null ? null : body.transferAmount(),
                        body == null ? null : body.referenceCode(),
                        authorization
                )
        );
        if (!ok) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(SePayWebhookResponse.fail());
        }
        return ResponseEntity.ok(SePayWebhookResponse.ok());
    }
}
