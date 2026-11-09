package com.group3.api.rest;

import com.group3.application.command.HandleSePayReturnCommand;
import com.group3.application.port.in.HandleSePayReturnPort;
import com.group3.infrastructure.config.SePayProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments/sepay/return")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SePayReturnController {

    SePayProperties sePayProperties;
    HandleSePayReturnPort handleSePayReturnPort;

    @GetMapping("/success")
    public ResponseEntity<Void> success(@RequestParam Map<String, String> params) {
        String invoice = param(params, "order_invoice_number");
        handleSePayReturnPort.handleSuccess(
                new HandleSePayReturnCommand(
                        invoice,
                        param(params, "order_id"),
                        param(params, "transaction_id")
                )
        );
        return redirect("SUCCESS", invoice);
    }

    @GetMapping("/error")
    public ResponseEntity<Void> error(@RequestParam Map<String, String> params) {
        return redirect("ERROR", param(params, "order_invoice_number"));
    }

    @GetMapping("/cancel")
    public ResponseEntity<Void> cancel(@RequestParam Map<String, String> params) {
        return redirect("CANCEL", param(params, "order_invoice_number"));
    }

    private ResponseEntity<Void> redirect(String status, String invoice) {
        String frontend = sePayProperties.getFrontendReturnUrl();
        if (frontend == null || frontend.isBlank()) {
            frontend = "http://localhost:5173/payment-return";
        }
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(frontend)
                .queryParam("status", status);
        if (invoice != null && !invoice.isBlank()) {
            builder.queryParam("invoice", invoice);
        }
        URI location = builder.build(true).toUri();
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, location.toString())
                .build();
    }

    private static String param(Map<String, String> params, String key) {
        if (params == null) {
            return null;
        }
        String value = params.get(key);
        return value == null || value.isBlank() ? null : value.trim();
    }
}
