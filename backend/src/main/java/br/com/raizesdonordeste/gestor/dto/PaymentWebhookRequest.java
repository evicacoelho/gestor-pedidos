package br.com.raizesdonordeste.gestor.dto;

import jakarta.validation.constraints.NotNull;

public record PaymentWebhookRequest(
        @NotNull String externalReference,
        boolean approved
) {
}
