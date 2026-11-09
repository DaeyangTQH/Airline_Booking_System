package com.group3.api.rest;

import com.group3.application.command.HandleSePayReturnCommand;
import com.group3.application.port.in.HandleSePayReturnPort;
import com.group3.infrastructure.config.SePayProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments/sepay/return")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SePayReturnController {

    SePayProperties sePayProperties;
    HandleSePayReturnPort handleSePayReturnPort;

    @GetMapping({"/success", "/success/{paymentId}"})
    public ResponseEntity<Void> success(
            @PathVariable(value = "paymentId", required = false) String paymentId,
            @RequestParam Map<String, String> params
    ) {
        String invoice = resolveInvoice(paymentId, params);
        String orderId = firstParam(params, "order_id", "orderId");
        String txnId = firstParam(params, "transaction_id", "transactionId");

        log.info("SePay success return paymentId={}, queryKeys={}", invoice, params.keySet());

        boolean completed = handleSePayReturnPort.handleSuccess(
                new HandleSePayReturnCommand(invoice, orderId, txnId)
        );
        return redirect(completed ? "SUCCESS" : "ERROR", invoice);
    }

    @GetMapping({"/error", "/error/{paymentId}"})
    public ResponseEntity<Void> error(
            @PathVariable(value = "paymentId", required = false) String paymentId,
            @RequestParam Map<String, String> params
    ) {
        return redirect("ERROR", resolveInvoice(paymentId, params));
    }

    @GetMapping({"/cancel", "/cancel/{paymentId}"})
    public ResponseEntity<Void> cancel(
            @PathVariable(value = "paymentId", required = false) String paymentId,
            @RequestParam Map<String, String> params
    ) {
        return redirect("CANCEL", resolveInvoice(paymentId, params));
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

    /**
     * Ưu tiên path paymentId (đã nhúng trong success_url); fallback query SePay gửi kèm.
     */
    private static String resolveInvoice(String pathPaymentId, Map<String, String> params) {
        if (pathPaymentId != null && !pathPaymentId.isBlank()) {
            return pathPaymentId.trim();
        }
        return firstParam(
                params,
                "order_invoice_number",
                "orderInvoiceNumber",
                "payment_id",
                "paymentId",
                "invoice"
        );
    }

    private static String firstParam(Map<String, String> params, String... keys) {
        if (params == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            String value = params.get(key);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}
