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

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentCheckoutController {

    PaymentRepository paymentRepository;
    SePayGatewayPort sePayGatewayPort;
    SePayProperties sePayProperties;

    @GetMapping(value = "/api/v1/payments/{paymentId}/checkout", produces = MediaType.TEXT_HTML_VALUE)
    public String checkout(@PathVariable("paymentId") UUID paymentId) {
        Payment payment = paymentRepository.findById(new PaymentId(paymentId))
                .orElseThrow(() -> new InvalidDomainValueException("Payment not found"));

        String code = sePayGatewayPort.paymentCode(payment);
        String qrUrl = sePayGatewayPort.buildQrImageUrl(payment);
        boolean vietQr = sePayGatewayPort.isVietQrSupported();
        boolean paid = payment.getStatus() == PaymentStatus.COMPLETED;
        String amount = formatVnd(payment.getAmount().amount());
        String holder = nullToDash(sePayProperties.getAccountHolder());
        String bank = nullToDash(sePayProperties.getBankCode());
        String account = nullToDash(sePayProperties.getAccountNumber());
        String statusLabel = paid ? "Thanh toán thành công" : "Đang chờ chuyển khoản";
        String statusClass = paid ? "ok" : "wait";
        String testHint = vietQr
                ? ""
                : """
                  <div class="hint">
                    <strong>SePay Test Mode (ACMEBank)</strong><br/>
                    VietQR thật không hỗ trợ ACMEBank. Hãy vào SePay →
                    <em>Mô phỏng giao dịch</em>, chọn tài khoản ACMEBank,
                    số tiền <code>%s</code>, nội dung chứa <code>%s</code>
                    rồi bấm mô phỏng để webhook về hệ thống.
                  </div>
                  """.formatted(payment.getAmount().amount().toPlainString(), code);

        return """
                <!doctype html>
                <html lang="vi">
                <head>
                  <meta charset="utf-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1"/>
                  <title>Thanh toán SePay</title>
                  <style>
                    :root { color-scheme: light; font-family: "Segoe UI", system-ui, sans-serif; }
                    body { margin: 0; min-height: 100vh; display: grid; place-items: center;
                           background: linear-gradient(160deg, #eef6ff, #f7fafc 45%%, #eef2ff); }
                    .card { width: min(100%% - 2rem, 26rem); background: #fff; border-radius: 1rem;
                            box-shadow: 0 10px 40px rgb(15 23 42 / 0.08); overflow: hidden; }
                    header { background: #0f766e; color: #fff; padding: 1.1rem 1.25rem; }
                    header h1 { margin: 0; font-size: 1.15rem; font-weight: 650; }
                    header p { margin: .35rem 0 0; opacity: .9; font-size: .9rem; }
                    main { padding: 1.25rem; display: grid; gap: 1rem; justify-items: center; }
                    img.qr { width: 16rem; height: 16rem; object-fit: contain; background: #fff;
                             border: 1px solid #e2e8f0; border-radius: .75rem; padding: .5rem; }
                    dl { width: 100%%; margin: 0; display: grid; grid-template-columns: 7.5rem 1fr;
                         gap: .45rem .75rem; font-size: .92rem; }
                    dt { color: #64748b; } dd { margin: 0; font-weight: 600; word-break: break-all; }
                    .status { width: 100%%; text-align: center; padding: .7rem; border-radius: .6rem; font-weight: 650; }
                    .status.wait { background: #fef3c7; color: #92400e; }
                    .status.ok { background: #d1fae5; color: #065f46; }
                    .hint { width: 100%%; background: #eff6ff; color: #1e3a8a; border-radius: .6rem;
                            padding: .8rem; font-size: .85rem; line-height: 1.45; }
                    code { background: #e2e8f0; padding: .1rem .3rem; border-radius: .25rem; }
                  </style>
                </head>
                <body>
                  <div class="card">
                    <header>
                      <h1>Thanh toán chuyển khoản</h1>
                      <p>Airline Booking · SePay</p>
                    </header>
                    <main>
                      <div class="status %s" id="status">%s</div>
                      <img class="qr" src="%s" alt="QR thanh toán"/>
                      <dl>
                        <dt>Ngân hàng</dt><dd>%s</dd>
                        <dt>Số tài khoản</dt><dd>%s</dd>
                        <dt>Chủ TK</dt><dd>%s</dd>
                        <dt>Số tiền</dt><dd>%s</dd>
                        <dt>Nội dung CK</dt><dd id="code">%s</dd>
                      </dl>
                      %s
                    </main>
                  </div>
                  <script>
                    const paid = %s;
                    const paymentId = "%s";
                    async function poll() {
                      if (paid) return;
                      try {
                        const res = await fetch("/api/v1/payments/" + paymentId + "/status");
                        const data = await res.json();
                        if (data.status === "COMPLETED") {
                          const el = document.getElementById("status");
                          el.className = "status ok";
                          el.textContent = "Thanh toán thành công";
                          return;
                        }
                      } catch (e) {}
                      setTimeout(poll, 3000);
                    }
                    poll();
                  </script>
                </body>
                </html>
                """.formatted(
                statusClass,
                statusLabel,
                escapeAttr(qrUrl),
                escapeHtml(bank),
                escapeHtml(account),
                escapeHtml(holder),
                escapeHtml(amount),
                escapeHtml(code),
                testHint,
                paid,
                paymentId
        );
    }

    private static String formatVnd(BigDecimal amount) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN")).format(amount);
    }

    private static String nullToDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private static String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String escapeAttr(String value) {
        return escapeHtml(value).replace("'", "&#39;");
    }
}
