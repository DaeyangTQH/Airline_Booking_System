package com.group3.application.port.out;

import com.group3.domain.aggregate.Payment;

import java.util.LinkedHashMap;

public interface SePayGatewayPort {

    /**
     * Mã hóa đơn gửi SePay ({@code order_invoice_number}) — dùng paymentId.
     */
    String invoiceNumber(Payment payment);

    /**
     * URL mở form auto-submit → trang thanh toán SePay Sandbox/Live.
     */
    String buildPayRedirectUrl(Payment payment);

    /**
     * Field form POST tới {@code checkout/init} (đã có signature).
     */
    LinkedHashMap<String, String> buildCheckoutFormFields(Payment payment);

    String checkoutInitUrl();
}
