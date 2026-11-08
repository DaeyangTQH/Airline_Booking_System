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
     * Số tài khoản / VA nhận tiền (VD: SBPAY... hoặc số TK).
     */
    private String accountNumber;

    /**
     * Short name ngân hàng. Test mode mặc định của SePay là {@code ACMEBank}
     * (không có trên VietQR thật). Live: MBBank, Vietcombank, BIDV, ...
     */
    private String bankCode;

    private String accountHolder = "";

    /**
     * Base URL tạo ảnh QR VietQR. ACMEBank sẽ không dùng URL này.
     */
    private String qrBaseUrl = "https://qr.sepay.vn/img";

    private String qrTemplate = "compact";

    /**
     * Public base của payment/gateway để mở trang checkout.
     * VD: http://localhost:8084 hoặc http://localhost:8080
     */
    private String checkoutBaseUrl = "http://localhost:8084";

    /**
     * API Key webhook — header Authorization: Apikey {key}
     */
    private String webhookApiKey;

    /**
     * Tiền tố mã thanh toán (khớp cấu hình SePay: Cấu hình mã thanh toán).
     */
    private String paymentCodePrefix = "AIR";

    /**
     * Độ dài hậu tố hex lấy từ paymentId (SePay cho phép tối đa ~30).
     */
    private int paymentCodeSuffixLength = 12;

    public boolean isVietQrSupported() {
        String bank = bankCode == null ? "" : bankCode.trim();
        return !bank.isEmpty() && !bank.equalsIgnoreCase("ACMEBank") && !bank.equalsIgnoreCase("ACME");
    }
}
