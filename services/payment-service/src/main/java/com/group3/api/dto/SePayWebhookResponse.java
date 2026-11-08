package com.group3.api.dto;

public record SePayWebhookResponse(boolean success) {

    public static SePayWebhookResponse ok() {
        return new SePayWebhookResponse(true);
    }

    public static SePayWebhookResponse fail() {
        return new SePayWebhookResponse(false);
    }
}
