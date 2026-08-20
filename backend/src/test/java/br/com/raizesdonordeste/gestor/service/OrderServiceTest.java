package br.com.raizesdonordeste.gestor.service;

import br.com.raizesdonordeste.gestor.domain.entity.*;
import br.com.raizesdonordeste.gestor.domain.enums.OrderChannel;
import br.com.raizesdonordeste.gestor.domain.enums.OrderStatus;
import br.com.raizesdonordeste.gestor.dto.CreateOrderRequest;
import br.com.raizesdonordeste.gestor.exception.InvalidOrderStateException;
import br.com.raizesdonordeste.gestor.exception.ProductUnavailableException;
import br.com.raizesdonordeste.gestor.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private UnitRepository unitRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductAvailabilityRepository availabilityRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    private Unit unit;
    private Product product;

    @BeforeEach
    void setUp() {
        unit = new Unit();
        unit.setName("Unidade Teste");
        unit.setCity("Recife");
        unit.setRegion("Nordeste");
        unit.setFullKitchen(true);
        unit = unitRepository.save(unit);

        product = new Product();
        product.setName("Tapioca");
        product.setPrice(new BigDecimal("15.00"));
        product = productRepository.save(product);

        ProductAvailability availability = new ProductAvailability();
        availability.setUnit(unit);
        availability.setProduct(product);
        availability.setAvailable(true);
        availability.setStockQuantity(10);
        availabilityRepository.save(availability);
    }

    @Test
    void deveCriarPedidoEAvancarAteEntrega() {
        Order order = orderService.createOrder(new CreateOrderRequest(
                unit.getId(), null, OrderChannel.TOTEM,
                List.of(new CreateOrderRequest.Item(product.getId(), 2))));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.AGUARDANDO_PAGAMENTO);
        assertThat(order.getTotal()).isEqualByComparingTo("30.00");

        order = orderService.confirmPayment(paymentReferenceOf(order), true);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAGO);

        order = orderService.advanceToPreparo(order.getId());
        order = orderService.advanceToPronto(order.getId());
        order = orderService.advanceToEntregue(order.getId());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ENTREGUE);
    }

    @Test
    void naoDevePermitirTransicaoDeCriadoDiretoParaEntregue() {
        Order order = orderService.createOrder(new CreateOrderRequest(
                unit.getId(), null, OrderChannel.TOTEM,
                List.of(new CreateOrderRequest.Item(product.getId(), 1))));

        // pedido esta em AGUARDANDO_PAGAMENTO; avancar para EM_PREPARO sem pagar deve falhar
        assertThatThrownBy(() -> orderService.advanceToPreparo(order.getId()))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void naoDevePermitirVendaAcimaDoEstoqueDaUnidade() {
        assertThatThrownBy(() -> orderService.createOrder(new CreateOrderRequest(
                unit.getId(), null, OrderChannel.APP,
                List.of(new CreateOrderRequest.Item(product.getId(), 999)))))
                .isInstanceOf(ProductUnavailableException.class);
    }

    @Test
    void webhookDePagamentoDeveSerIdempotente() {
        Order order = orderService.createOrder(new CreateOrderRequest(
                unit.getId(), null, OrderChannel.APP,
                List.of(new CreateOrderRequest.Item(product.getId(), 1))));
        String reference = paymentReferenceOf(order);

        orderService.confirmPayment(reference, true);
        Order afterSecondCall = orderService.confirmPayment(reference, true);

        assertThat(afterSecondCall.getStatus()).isEqualTo(OrderStatus.PAGO);
    }

    private String paymentReferenceOf(Order order) {
        return paymentRepository.findByOrderId(order.getId())
                .orElseThrow()
                .getExternalReference();
    }
}
