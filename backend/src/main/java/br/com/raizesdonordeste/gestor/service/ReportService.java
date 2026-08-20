package br.com.raizesdonordeste.gestor.service;

import br.com.raizesdonordeste.gestor.domain.entity.Order;
import br.com.raizesdonordeste.gestor.domain.entity.OrderItem;
import br.com.raizesdonordeste.gestor.domain.enums.OrderStatus;
import br.com.raizesdonordeste.gestor.dto.SalesByUnitDto;
import br.com.raizesdonordeste.gestor.dto.TopProductDto;
import br.com.raizesdonordeste.gestor.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class ReportService {

    private static final List<OrderStatus> REVENUE_STATUSES =
            List.of(OrderStatus.PAGO, OrderStatus.EM_PREPARO, OrderStatus.PRONTO, OrderStatus.ENTREGUE);

    private final OrderRepository orderRepository;

    public ReportService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<SalesByUnitDto> salesByUnit() {
        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> REVENUE_STATUSES.contains(o.getStatus()))
                .toList();

        Map<String, List<Order>> byUnit = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getUnit().getName()));

        return byUnit.entrySet().stream()
                .map(e -> new SalesByUnitDto(
                        e.getKey(),
                        e.getValue().size(),
                        e.getValue().stream().map(Order::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add)))
                .sorted(Comparator.comparing(SalesByUnitDto::totalRevenue).reversed())
                .toList();
    }

    public List<TopProductDto> topProducts(int limit) {
        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> REVENUE_STATUSES.contains(o.getStatus()))
                .toList();

        Map<String, Integer> quantityByProduct = orders.stream()
                .flatMap(o -> o.getItems().stream())
                .collect(Collectors.groupingBy(
                        i -> i.getProduct().getName(),
                        Collectors.summingInt(OrderItem::getQuantity)));

        return quantityByProduct.entrySet().stream()
                .map(e -> new TopProductDto(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(TopProductDto::quantitySold).reversed())
                .limit(limit)
                .toList();
    }
}
