package br.com.raizesdonordeste.gestor.controller;

import br.com.raizesdonordeste.gestor.domain.entity.Order;
import br.com.raizesdonordeste.gestor.dto.*;
import br.com.raizesdonordeste.gestor.exception.ResourceNotFoundException;
import br.com.raizesdonordeste.gestor.repository.OrderRepository;
import br.com.raizesdonordeste.gestor.repository.PaymentRepository;
import br.com.raizesdonordeste.gestor.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public OrderController(OrderService orderService, OrderRepository orderRepository,
                            PaymentRepository paymentRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        String reference = paymentRepository.findByOrderId(order.getId())
                .map(p -> p.getExternalReference())
                .orElse(null);
        return OrderResponse.from(order, reference);
    }

    @GetMapping("/{orderId}")
    public OrderResponse get(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido nao encontrado: " + orderId));
        return OrderResponse.from(order);
    }

    @PostMapping("/{orderId}/advance-to-preparo")
    public OrderResponse advanceToPreparo(@PathVariable Long orderId) {
        return OrderResponse.from(orderService.advanceToPreparo(orderId));
    }

    @PostMapping("/{orderId}/advance-to-pronto")
    public OrderResponse advanceToPronto(@PathVariable Long orderId) {
        return OrderResponse.from(orderService.advanceToPronto(orderId));
    }

    @PostMapping("/{orderId}/advance-to-entregue")
    public OrderResponse advanceToEntregue(@PathVariable Long orderId) {
        return OrderResponse.from(orderService.advanceToEntregue(orderId));
    }

    @PostMapping("/{orderId}/cancel")
    public OrderResponse cancel(@PathVariable Long orderId, @Valid @RequestBody CancelOrderRequest request) {
        return OrderResponse.from(orderService.cancelOrder(orderId, request.reason(), request.performedBy()));
    }

    @PostMapping("/{orderId}/discount")
    public OrderResponse applyDiscount(@PathVariable Long orderId, @Valid @RequestBody DiscountRequest request) {
        return OrderResponse.from(
                orderService.applyDiscount(orderId, request.amount(), request.reason(), request.performedBy()));
    }
}
