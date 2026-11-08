package com.group3.infrastructure.adapter;

import com.group3.application.port.out.SePayGatewayPort;
import com.group3.domain.aggregate.Payment;
import com.group3.infrastructure.config.SePayProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SePayGatewayAdapter implements SePayGatewayPort {

    private static final Pattern UUID_HEX = Pattern.compile(
            "(?i)(?:^|[^A-F0-9])([A-F0-9]{8}-?[A-F0-9]{4}-?[A-F0-9]{4}-?[A-F0-9]{4}-?[A-F0-9]{12})(?:[^A-F0-9]|$)"
    );

    SePayProperties properties;

    @Override
    public String paymentCode(Payment payment) {
        String hex = payment.getId().value().toString().replace("-", "").toUpperCase(Locale.ROOT);
        int len = Math.min(Math.max(properties.getPaymentCodeSuffixLength(), 8), hex.length());
        String suffix = hex.substring(0, len);
        String prefix = properties.getPaymentCodePrefix();
        if (prefix == null || prefix.isBlank()) {
            return suffix;
        }
        return prefix.trim().toUpperCase(Locale.ROOT) + suffix;
    }

    @Override
    public String buildCheckoutUrl(Payment payment) {
        String base = properties.getCheckoutBaseUrl();
        if (base == null || base.isBlank()) {
            base = "http://localhost:8084";
        }
        return UriComponentsBuilder
                .fromUriString(base.replaceAll("/$", ""))
                .path("/api/v1/payments/{id}/checkout")
                .buildAndExpand(payment.getId().value())
                .toUriString();
    }

    @Override
    public String buildQrImageUrl(Payment payment) {
        requireConfigured();
        if (isVietQrSupported()) {
            return buildVietQrUrl(payment);
        }
        // ACMEBank / Test Mode: VietQR không hỗ trợ → QR text hướng dẫn chuyển khoản
        String payload = String.join("\n",
                "SePay Test Mode",
                "Bank: " + properties.getBankCode(),
                "Acc: " + properties.getAccountNumber(),
                "Amount: " + payment.getAmount().amount().toPlainString() + " VND",
                "Content: " + paymentCode(payment)
        );
        return "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data="
                + UriUtils.encodeQueryParam(payload, StandardCharsets.UTF_8);
    }

    @Override
    public boolean isVietQrSupported() {
        return properties.isVietQrSupported();
    }

    @Override
    public Optional<UUID> parsePaymentId(String codeOrContent) {
        if (codeOrContent == null || codeOrContent.isBlank()) {
            return Optional.empty();
        }
        Optional<String> suffix = extractCodeSuffix(codeOrContent);
        if (suffix.isPresent() && suffix.get().length() == 32) {
            return tryParseUuid(suffix.get());
        }
        Matcher matcher = UUID_HEX.matcher(codeOrContent);
        if (matcher.find()) {
            return tryParseUuid(matcher.group(1));
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> extractCodeSuffix(String codeOrContent) {
        if (codeOrContent == null || codeOrContent.isBlank()) {
            return Optional.empty();
        }
        String text = codeOrContent.trim().toUpperCase(Locale.ROOT);
        String prefix = properties.getPaymentCodePrefix();
        if (prefix != null && !prefix.isBlank()) {
            String p = prefix.trim().toUpperCase(Locale.ROOT);
            Pattern prefixed = Pattern.compile(
                    "(?i)\\b" + Pattern.quote(p) + "([A-F0-9]{8,32})\\b"
            );
            Matcher m = prefixed.matcher(text.replace("-", ""));
            if (m.find()) {
                return Optional.of(m.group(1).toUpperCase(Locale.ROOT));
            }
            // code field may be exactly PREFIX+suffix
            String compact = text.replace("-", "").replace(" ", "");
            if (compact.startsWith(p) && compact.length() > p.length()) {
                return Optional.of(compact.substring(p.length()));
            }
        }
        Matcher matcher = UUID_HEX.matcher(codeOrContent);
        if (matcher.find()) {
            return Optional.of(matcher.group(1).replace("-", "").toUpperCase(Locale.ROOT));
        }
        return Optional.empty();
    }

    private String buildVietQrUrl(Payment payment) {
        long amountVnd = payment.getAmount().amount().longValueExact();
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(properties.getQrBaseUrl())
                .queryParam("acc", properties.getAccountNumber())
                .queryParam("bank", properties.getBankCode())
                .queryParam("amount", amountVnd)
                .queryParam("des", paymentCode(payment));

        String template = properties.getQrTemplate();
        if (template != null && !template.isBlank()) {
            builder.queryParam("template", template);
        }
        return builder.build(true).toUriString();
    }

    private void requireConfigured() {
        if (properties.getAccountNumber() == null || properties.getAccountNumber().isBlank()) {
            throw new IllegalStateException("app.sepay.account-number / SEPAY_ACCOUNT_NUMBER is required");
        }
        if (properties.getBankCode() == null || properties.getBankCode().isBlank()) {
            throw new IllegalStateException("app.sepay.bank-code / SEPAY_BANK_CODE is required");
        }
    }

    private static Optional<UUID> tryParseUuid(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String v = value.trim();
        try {
            if (v.length() == 32 && v.indexOf('-') < 0) {
                String dashed = v.replaceFirst(
                        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{12})",
                        "$1-$2-$3-$4-$5"
                );
                return Optional.of(UUID.fromString(dashed));
            }
            return Optional.of(UUID.fromString(v));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
