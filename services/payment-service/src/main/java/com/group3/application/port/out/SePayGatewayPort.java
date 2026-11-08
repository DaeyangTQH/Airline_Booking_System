package com.group3.application.port.out;

import com.group3.domain.aggregate.Payment;

import java.util.Optional;
import java.util.UUID;

public interface SePayGatewayPort {

    String paymentCode(Payment payment);

    /**
     * URL trang checkout (hiển thị QR / thông tin CK). Đây là {@code paymentUrl} trả về client.
     */
    String buildCheckoutUrl(Payment payment);

    /**
     * URL ảnh QR (VietQR nếu bank được hỗ trợ; fallback QR text cho ACMEBank Test Mode).
     */
    String buildQrImageUrl(Payment payment);

    /**
     * Parse mã / nội dung CK → UUID đầy đủ nếu mã chứa full UUID;
     * nếu chỉ có hex ngắn thì trả empty (caller lookup theo prefix).
     */
    Optional<UUID> parsePaymentId(String codeOrContent);

    /**
     * Hex hậu tố trong mã thanh toán (không gồm prefix), dùng lookup DB.
     */
    Optional<String> extractCodeSuffix(String codeOrContent);

    boolean isVietQrSupported();
}
