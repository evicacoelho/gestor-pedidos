package br.com.raizesdonordeste.gestor.dto;

import br.com.raizesdonordeste.gestor.domain.entity.Order;
import br.com.raizesdonordeste.gestor.domain.enums.OrderChannel;
import br.com.raizesdonordeste.gestor.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        Long id,
        Long unitId,
        OrderChannel channel,
        OrderStatus status,
        BigDecimal total,
        List<ItemView> items,
        String paymentReference
) {
    public record ItemView(String productName, int quantity, BigDecimal unitPrice) {
    }

    public static OrderResponse from(Order order) {
        return from(order, null);
    }

    /**
     * paymentReference so existe para permitir que a UI de demonstracao dispare
     * manualmente o webhook de confirmacao (ver frontend); em um gateway real
     * essa referencia nunca seria devolvida ao cliente final.
     */
    public static OrderResponse from(Order order, String paymentReference) {
        List<ItemView> items = order.getItems().stream()
                .map(i -> new ItemView(i.getProduct().getName(), i.getQuantity(), i.getUnitPrice()))
                .toList();
        return new OrderResponse(order.getId(), order.getUnit().getId(), order.getChannel(),
                order.getStatus(), order.getTotal(), items, paymentReference);
    }
}
