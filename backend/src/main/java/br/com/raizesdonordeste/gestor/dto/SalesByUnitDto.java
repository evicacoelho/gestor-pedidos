package br.com.raizesdonordeste.gestor.dto;

import java.math.BigDecimal;

public record SalesByUnitDto(String unitName, int orderCount, BigDecimal totalRevenue) {
}
