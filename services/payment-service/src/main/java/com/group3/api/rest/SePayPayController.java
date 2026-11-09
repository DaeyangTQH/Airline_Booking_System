package com.group3.api.rest;

import com.group3.application.port.in.GetPayCheckoutPagePort;
import com.group3.application.result.PayCheckoutPageResult;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * Cầu mỏng: mở URL này → auto POST form sang trang thanh toán SePay Sandbox/Live.
 */
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SePayPayController {

    GetPayCheckoutPagePort getPayCheckoutPagePort;

    @GetMapping(value = "/api/v1/payments/{paymentId}/pay", produces = MediaType.TEXT_HTML_VALUE)
    public String pay(@PathVariable("paymentId") UUID paymentId) {
        PayCheckoutPageResult result = getPayCheckoutPagePort.getCheckoutPage(paymentId);

        if (result.alreadyPaid()) {
            String location = escapeAttr(result.redirectUrl());
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

        String action = escapeAttr(result.formAction());
        StringBuilder inputs = new StringBuilder();
        if (result.formFields() != null) {
            for (Map.Entry<String, String> entry : result.formFields().entrySet()) {
                inputs.append("<input type=\"hidden\" name=\"")
                        .append(escapeAttr(entry.getKey()))
                        .append("\" value=\"")
                        .append(escapeAttr(entry.getValue()))
                        .append("\"/>\n");
            }
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
