package br.com.raizesdonordeste.gestor.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;


@Service
public class MockPaymentGatewayAdapter implements PaymentGatewayAdapter {

    @Override
    public String requestPayment(Long orderId, BigDecimal amount) {
        return "PAY-" + orderId + "-" + UUID.randomUUID();
    }
}
