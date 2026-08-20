package br.com.raizesdonordeste.gestor.service;

import java.math.BigDecimal;

public interface PaymentGatewayAdapter {
    String requestPayment(Long orderId, BigDecimal amount);
}
