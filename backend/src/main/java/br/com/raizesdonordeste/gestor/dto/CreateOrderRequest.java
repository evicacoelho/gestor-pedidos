package br.com.raizesdonordeste.gestor.dto;

import br.com.raizesdonordeste.gestor.domain.enums.OrderChannel;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotNull Long unitId,
        Long customerId,
        @NotNull OrderChannel channel,
        @NotEmpty List<Item> items
) {
    public record Item(@NotNull Long productId, int quantity) {
    }
}
