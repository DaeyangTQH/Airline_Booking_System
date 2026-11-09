package com.group3.application.result;

import java.util.Map;

public record PayCheckoutPageResult(
        boolean alreadyPaid,
        String redirectUrl,
        String formAction,
        Map<String, String> formFields
) {
    public static PayCheckoutPageResult alreadyPaid(String redirectUrl) {
        return new PayCheckoutPageResult(true, redirectUrl, null, Map.of());
    }

    public static PayCheckoutPageResult checkout(String formAction, Map<String, String> formFields) {
        return new PayCheckoutPageResult(false, null, formAction, formFields);
    }
}
