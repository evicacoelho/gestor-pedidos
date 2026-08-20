package br.com.raizesdonordeste.gestor.repository;

import br.com.raizesdonordeste.gestor.domain.entity.ProductAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductAvailabilityRepository extends JpaRepository<ProductAvailability, Long> {

    List<ProductAvailability> findByUnitIdAndAvailableTrue(Long unitId);
}
