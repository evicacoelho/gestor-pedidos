package br.com.raizesdonordeste.gestor.dto;

import java.math.BigDecimal;

public record ProductView(Long productId, String name, BigDecimal price, int stockQuantity) {
}
