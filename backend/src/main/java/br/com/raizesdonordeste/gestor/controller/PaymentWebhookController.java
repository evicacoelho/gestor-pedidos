package br.com.raizesdonordeste.gestor.controller;

import br.com.raizesdonordeste.gestor.dto.OrderResponse;
import br.com.raizesdonordeste.gestor.dto.PaymentWebhookRequest;
import br.com.raizesdonordeste.gestor.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/payments/webhook")
public class PaymentWebhookController {

    private final OrderService orderService;

    public PaymentWebhookController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderResponse receive(@Valid @RequestBody PaymentWebhookRequest request) {
        return OrderResponse.from(orderService.confirmPayment(request.externalReference(), request.approved()));
    }
}
