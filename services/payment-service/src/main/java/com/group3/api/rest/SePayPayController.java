package com.group3.api.rest;

import com.group3.application.port.out.SePayGatewayPort;
import com.group3.domain.aggregate.Payment;
import com.group3.domain.aggregate.PaymentStatus;
import com.group3.domain.exception.InvalidDomainValueException;
import com.group3.domain.repository.PaymentRepository;
import com.group3.domain.vo.PaymentId;
import com.group3.infrastructure.config.SePayProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Cầu mỏng: mở URL này → auto POST form sang trang thanh toán SePay Sandbox/Live.
 */
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SePayPayController {

    PaymentRepository paymentRepository;
    SePayGatewayPort sePayGatewayPort;
    SePayProperties sePayProperties;

    @GetMapping(value = "/api/v1/payments/{paymentId}/pay", produces = MediaType.TEXT_HTML_VALUE)
    public String pay(@PathVariable("paymentId") UUID paymentId) {
        Payment payment = paymentRepository.findById(new PaymentId(paymentId))
                .orElseThrow(() -> new InvalidDomainValueException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            String frontend = sePayProperties.getFrontendReturnUrl();
            if (frontend == null || frontend.isBlank()) {
                frontend = "http://localhost:5173/payment-return";
            }
            String location = escapeAttr(frontend + "?status=SUCCESS&invoice=" + paymentId);
            return """
                    <!doctype html>
                    <html lang="vi">
                    <head>
                      <meta charset="utf-8"/>
                      <meta http-equiv="refresh" content="0;url=%s"/>
                      <title>Payment already completed</title>
                    </head>
                    <body>
                      <p>Thanh toán này đã hoàn tất trước đó.</p>
                      <p><a href="%s">Tiếp tục</a></p>
                    </body>
                    </html>
                    """.formatted(location, location);
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidDomainValueException(
                    "Payment is not payable in status " + payment.getStatus()
            );
        }

        LinkedHashMap<String, String> fields = sePayGatewayPort.buildCheckoutFormFields(payment);
        String action = escapeAttr(sePayGatewayPort.checkoutInitUrl());

        StringBuilder inputs = new StringBuilder();
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            inputs.append("<input type=\"hidden\" name=\"")
                    .append(escapeAttr(entry.getKey()))
                    .append("\" value=\"")
                    .append(escapeAttr(entry.getValue()))
                    .append("\"/>\n");
        }

        return """
                <!doctype html>
                <html lang="vi">
                <head>
                  <meta charset="utf-8"/>
                  <meta http-equiv="Cache-Control" content="no-store"/>
                  <title>Redirecting to SePay...</title>
                </head>
                <body>
                  <p>Đang chuyển tới trang thanh toán SePay...</p>
                  <form id="sepay" method="POST" action="%s">
                    %s
                    <noscript><button type="submit">Tiếp tục thanh toán</button></noscript>
                  </form>
                  <script>document.getElementById("sepay").submit();</script>
                </body>
                </html>
                """.formatted(action, inputs);
    }

    private static String escapeAttr(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
