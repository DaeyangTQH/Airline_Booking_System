package com.group3.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.sepay")
public class SePayProperties {

    /**
     * Merchant ID từ Cổng thanh toán SePay (Sandbox / Live).
     */
    private String merchantId;

    /**
     * Secret Key ký form — {@code spsk_test_...} (sandbox) hoặc {@code spsk_live_...}.
     */
    private String secretKey;

    /**
     * Sandbox: https://pay-sandbox.sepay.vn/v1/checkout/init
     * Live: https://pay.sepay.vn/v1/checkout/init
     */
    private String checkoutInitUrl = "https://pay-sandbox.sepay.vn/v1/checkout/init";

    /**
     * Base URL của payment-service (success/error/cancel).
     */
    private String callbackBaseUrl = "http://localhost:8080";

    /**
     * FE nhận kết quả sau khi SePay redirect về.
     */
    private String frontendReturnUrl = "http://localhost:5173/payment-return";

    /**
     * CARD | BANK_TRANSFER | NAPAS_BANK_TRANSFER
     */
    private String paymentMethod = "BANK_TRANSFER";

    private String currency = "VND";

    private String lang = "vi";
}
