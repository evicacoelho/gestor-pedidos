package br.com.raizesdonordeste.gestor.repository;

import br.com.raizesdonordeste.gestor.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUnitId(Long unitId);
}
