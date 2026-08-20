package br.com.raizesdonordeste.gestor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DiscountRequest(
        @NotNull @Positive BigDecimal amount,
        @NotBlank String reason,
        @NotBlank String performedBy
) {
}
