package com.group3.infrastructure.adapter;

import com.group3.application.port.out.SePayGatewayPort;
import com.group3.domain.aggregate.Payment;
import com.group3.infrastructure.config.SePayProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SePayGatewayAdapter implements SePayGatewayPort {

    /**
     * Allowlist ký form — khớp SDK Node SePay.
     * Quan trọng: ký theo <b>thứ tự field trong form</b> (LinkedHashMap),
     * không dùng thứ tự cố định của docs PHP (docs PHP làm signature sai → "Yêu cầu không hợp lệ").
     */
    private static final Set<String> SIGNED_FIELDS = Set.of(
            "merchant",
            "env",
            "operation",
            "payment_method",
            "order_amount",
            "currency",
            "order_invoice_number",
            "order_description",
            "customer_id",
            "agreement_id",
            "agreement_name",
            "agreement_type",
            "agreement_payment_frequency",
            "agreement_amount_per_payment",
            "success_url",
            "error_url",
            "cancel_url",
            "order_id"
    );

    SePayProperties properties;

    @Override
    public String invoiceNumber(Payment payment) {
        return payment.getId().value().toString();
    }

    @Override
    public String buildPayRedirectUrl(Payment payment) {
        String base = properties.getCallbackBaseUrl();
        if (base == null || base.isBlank()) {
            base = "http://localhost:8080";
        }
        return UriComponentsBuilder
                .fromUriString(base.replaceAll("/$", ""))
                .path("/api/v1/payments/{id}/pay")
                .buildAndExpand(payment.getId().value())
                .toUriString();
    }

    @Override
    public LinkedHashMap<String, String> buildCheckoutFormFields(Payment payment) {
        requireConfigured();
        String base = properties.getCallbackBaseUrl().replaceAll("/$", "");
        String invoice = invoiceNumber(payment);
        String amount = payment.getAmount().amount().toBigInteger().toString();

        // Invoice phải unique mỗi lần mở checkout. SePay nếu nhận lại invoice đã paid
        // sẽ mở lại đơn cũ → hiện "thanh toán thành công" dù user tưởng tạo đơn mới.
        String checkoutInvoice = invoice + "-" + System.nanoTime();

        // Thứ tự input = thứ tự ký (SDK Node / form mẫu SePay)
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put("merchant", properties.getMerchantId().trim());
        fields.put("operation", "PURCHASE");
        String method = blankToDefault(properties.getPaymentMethod(), "BANK_TRANSFER");
        fields.put("payment_method", method);
        fields.put("order_invoice_number", checkoutInvoice);
        fields.put("order_amount", amount);
        fields.put("currency", blankToDefault(properties.getCurrency(), "VND"));
        fields.put("order_description", "Thanh toan booking " + invoice);
        // SePay thường chỉ redirect về success_url, không luôn kèm order_invoice_number.
        // Nhúng payment_id vào URL để return handler luôn biết payment nào cần complete.
        fields.put("success_url", returnUrl(base, "success", invoice));
        fields.put("error_url", returnUrl(base, "error", invoice));
        fields.put("cancel_url", returnUrl(base, "cancel", invoice));
        if (properties.getLang() != null && !properties.getLang().isBlank()) {
            fields.put("lang", properties.getLang().trim());
        }
        fields.put("signature", signFields(fields));
        return fields;
    }

    @Override
    public String checkoutInitUrl() {
        return properties.getCheckoutInitUrl();
    }

    /**
     * Ký theo thứ tự xuất hiện của field trong map (giống Object.keys của SDK Node).
     */
    String signFields(Map<String, String> fields) {
        StringBuilder signed = new StringBuilder();
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            String field = entry.getKey();
            String value = entry.getValue();
            if (!SIGNED_FIELDS.contains(field) || value == null) {
                continue;
            }
            if (!signed.isEmpty()) {
                signed.append(',');
            }
            signed.append(field).append('=').append(value);
        }
        return hmacSha256Base64(signed.toString(), properties.getSecretKey());
    }

    private void requireConfigured() {
        if (properties.getMerchantId() == null || properties.getMerchantId().isBlank()) {
            throw new IllegalStateException("SEPAY_MERCHANT_ID is required");
        }
        if (properties.getSecretKey() == null || properties.getSecretKey().isBlank()) {
            throw new IllegalStateException("SEPAY_SECRET_KEY is required");
        }
        if (properties.getCheckoutInitUrl() == null || properties.getCheckoutInitUrl().isBlank()) {
            throw new IllegalStateException("SEPAY_CHECKOUT_INIT_URL is required");
        }
        if (properties.getCallbackBaseUrl() == null || properties.getCallbackBaseUrl().isBlank()) {
            throw new IllegalStateException("SEPAY_CALLBACK_BASE_URL is required");
        }
    }

    private static String returnUrl(String base, String path, String paymentId) {
        // Path-based id: SePay thường append query (?order_id=...) — tránh đụng query sẵn có.
        return base + "/api/v1/payments/sepay/return/" + path + "/" + paymentId;
    }

    private static String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static String hmacSha256Base64(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getEncoder().encodeToString(
                    mac.doFinal(data.getBytes(StandardCharsets.UTF_8))
            );
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot sign SePay checkout fields", ex);
        }
    }
}
