package br.com.raizesdonordeste.gestor.controller;

import br.com.raizesdonordeste.gestor.dto.ProductView;
import br.com.raizesdonordeste.gestor.repository.ProductAvailabilityRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/units/{unitId}/menu")
public class ProductController {

    private final ProductAvailabilityRepository availabilityRepository;

    public ProductController(ProductAvailabilityRepository availabilityRepository) {
        this.availabilityRepository = availabilityRepository;
    }

    @GetMapping
    public List<ProductView> menu(@PathVariable Long unitId) {
        return availabilityRepository.findByUnitIdAndAvailableTrue(unitId).stream()
                .map(a -> new ProductView(
                        a.getProduct().getId(),
                        a.getProduct().getName(),
                        a.getProduct().getPrice(),
                        a.getStockQuantity()))
                .toList();
    }
}
