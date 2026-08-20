package br.com.raizesdonordeste.gestor.service;

import br.com.raizesdonordeste.gestor.domain.entity.*;
import br.com.raizesdonordeste.gestor.domain.enums.AuditAction;
import br.com.raizesdonordeste.gestor.domain.enums.OrderStatus;
import br.com.raizesdonordeste.gestor.domain.enums.PaymentStatus;
import br.com.raizesdonordeste.gestor.dto.CreateOrderRequest;
import br.com.raizesdonordeste.gestor.exception.InvalidOrderStateException;
import br.com.raizesdonordeste.gestor.exception.ProductUnavailableException;
import br.com.raizesdonordeste.gestor.exception.ResourceNotFoundException;
import br.com.raizesdonordeste.gestor.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;


@Service
public class OrderService {

    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        VALID_TRANSITIONS.put(OrderStatus.CRIADO, EnumSet.of(OrderStatus.AGUARDANDO_PAGAMENTO, OrderStatus.CANCELADO));
        VALID_TRANSITIONS.put(OrderStatus.AGUARDANDO_PAGAMENTO, EnumSet.of(OrderStatus.PAGO, OrderStatus.CANCELADO));
        VALID_TRANSITIONS.put(OrderStatus.PAGO, EnumSet.of(OrderStatus.EM_PREPARO, OrderStatus.CANCELADO));
        VALID_TRANSITIONS.put(OrderStatus.EM_PREPARO, EnumSet.of(OrderStatus.PRONTO, OrderStatus.CANCELADO));
        VALID_TRANSITIONS.put(OrderStatus.PRONTO, EnumSet.of(OrderStatus.ENTREGUE, OrderStatus.CANCELADO));
        VALID_TRANSITIONS.put(OrderStatus.ENTREGUE, EnumSet.noneOf(OrderStatus.class));
        VALID_TRANSITIONS.put(OrderStatus.CANCELADO, EnumSet.noneOf(OrderStatus.class));
    }

    private final OrderRepository orderRepository;
    private final UnitRepository unitRepository;
    private final ProductRepository productRepository;
    private final ProductAvailabilityRepository availabilityRepository;
    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayAdapter paymentGatewayAdapter;
    private final LoyaltyService loyaltyService;
    private final AuditService auditService;

    public OrderService(OrderRepository orderRepository, UnitRepository unitRepository,
                         ProductRepository productRepository, ProductAvailabilityRepository availabilityRepository,
                         CustomerRepository customerRepository, PaymentRepository paymentRepository,
                         PaymentGatewayAdapter paymentGatewayAdapter, LoyaltyService loyaltyService,
                         AuditService auditService) {
        this.orderRepository = orderRepository;
        this.unitRepository = unitRepository;
        this.productRepository = productRepository;
        this.availabilityRepository = availabilityRepository;
        this.customerRepository = customerRepository;
        this.paymentRepository = paymentRepository;
        this.paymentGatewayAdapter = paymentGatewayAdapter;
        this.loyaltyService = loyaltyService;
        this.auditService = auditService;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Unit unit = unitRepository.findById(request.unitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unidade nao encontrada: " + request.unitId()));

        Order order = new Order();
        order.setUnit(unit);
        order.setChannel(request.channel());
        if (request.customerId() != null) {
            Customer customer = customerRepository.findById(request.customerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente nao encontrado: " + request.customerId()));
            order.setCustomer(customer);
        }

        for (CreateOrderRequest.Item itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado: " + itemRequest.productId()));

            ProductAvailability availability = availabilityRepository.findByUnitIdAndAvailableTrue(unit.getId()).stream()
                    .filter(a -> a.getProduct().getId().equals(product.getId()))
                    .findFirst()
                    .orElseThrow(() -> new ProductUnavailableException(
                            "Produto '" + product.getName() + "' nao disponivel na unidade " + unit.getName()));

            if (availability.getStockQuantity() < itemRequest.quantity()) {
                throw new ProductUnavailableException(
                        "Estoque insuficiente de '" + product.getName() + "' na unidade " + unit.getName());
            }
            availability.setStockQuantity(availability.getStockQuantity() - itemRequest.quantity());

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(itemRequest.quantity());
            item.setUnitPrice(product.getPrice());
            order.addItem(item);
        }

        order.recalculateTotal();
        order = orderRepository.save(order);

        transition(order, OrderStatus.AGUARDANDO_PAGAMENTO);
        String externalReference = paymentGatewayAdapter.requestPayment(order.getId(), order.getTotal());

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setExternalReference(externalReference);
        payment.setAmount(order.getTotal());
        paymentRepository.save(payment);

        return orderRepository.save(order);
    }

    /**
     * Processa a notificacao do gateway de pagamento. Idempotente: se o
     * webhook for reentregue para uma referencia ja processada, a segunda
     * chamada e ignorada sem efeito colateral (fundamental em integracoes
     * assincronas, que costumam reenviar notificacoes).
     */
    @Transactional
    public Order confirmPayment(String externalReference, boolean approved) {
        Payment payment = paymentRepository.findByExternalReference(externalReference)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento nao encontrado: " + externalReference));

        if (payment.getStatus() != PaymentStatus.PENDENTE) {
            return payment.getOrder(); // ja processado - noop idempotente
        }

        Order order = payment.getOrder();
        payment.setProcessedAt(Instant.now());

        if (approved) {
            payment.setStatus(PaymentStatus.APROVADO);
            transition(order, OrderStatus.PAGO);
            loyaltyService.accruePointsForOrder(order.getCustomer(), order.getTotal());
        } else {
            payment.setStatus(PaymentStatus.RECUSADO);
            transition(order, OrderStatus.CANCELADO);
            auditService.record(order, AuditAction.CANCELAMENTO, "Pagamento recusado pelo gateway", "sistema");
        }

        paymentRepository.save(payment);
        return orderRepository.save(order);
    }

    @Transactional
    public Order advanceToPreparo(Long orderId) {
        return transitionById(orderId, OrderStatus.EM_PREPARO);
    }

    @Transactional
    public Order advanceToPronto(Long orderId) {
        return transitionById(orderId, OrderStatus.PRONTO);
    }

    @Transactional
    public Order advanceToEntregue(Long orderId) {
        return transitionById(orderId, OrderStatus.ENTREGUE);
    }

    @Transactional
    public Order cancelOrder(Long orderId, String reason, String performedBy) {
        Order order = findOrder(orderId);
        transition(order, OrderStatus.CANCELADO);
        auditService.record(order, AuditAction.CANCELAMENTO, reason, performedBy);
        return orderRepository.save(order);
    }

    @Transactional
    public Order applyDiscount(Long orderId, BigDecimal amount, String reason, String performedBy) {
        Order order = findOrder(orderId);
        if (order.getStatus() == OrderStatus.ENTREGUE || order.getStatus() == OrderStatus.CANCELADO) {
            throw new InvalidOrderStateException("Nao e possivel aplicar desconto em pedido " + order.getStatus());
        }
        order.setDiscountApplied(amount);
        order.recalculateTotal();
        auditService.record(order, AuditAction.DESCONTO_APLICADO, reason, performedBy);
        return orderRepository.save(order);
    }

    private Order transitionById(Long orderId, OrderStatus target) {
        Order order = findOrder(orderId);
        transition(order, target);
        return orderRepository.save(order);
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido nao encontrado: " + orderId));
    }

    private void transition(Order order, OrderStatus target) {
        Set<OrderStatus> allowed = VALID_TRANSITIONS.get(order.getStatus());
        if (allowed == null || !allowed.contains(target)) {
            throw new InvalidOrderStateException(
                    "Transicao invalida de " + order.getStatus() + " para " + target);
        }
        order.setStatus(target);
        order.setUpdatedAt(Instant.now());
    }
}
