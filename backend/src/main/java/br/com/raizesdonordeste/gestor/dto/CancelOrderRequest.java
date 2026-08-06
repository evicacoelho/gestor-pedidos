package br.com.raizesdonordeste.gestor.dto;

import jakarta.validation.constraints.NotBlank;

public record CancelOrderRequest(
        @NotBlank String reason,
        @NotBlank String performedBy
) {
}
