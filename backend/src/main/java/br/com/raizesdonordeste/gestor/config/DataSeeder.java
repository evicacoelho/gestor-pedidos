package br.com.raizesdonordeste.gestor.config;

import br.com.raizesdonordeste.gestor.domain.entity.Product;
import br.com.raizesdonordeste.gestor.domain.entity.ProductAvailability;
import br.com.raizesdonordeste.gestor.domain.entity.Unit;
import br.com.raizesdonordeste.gestor.repository.ProductAvailabilityRepository;
import br.com.raizesdonordeste.gestor.repository.ProductRepository;
import br.com.raizesdonordeste.gestor.repository.UnitRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UnitRepository unitRepository;
    private final ProductRepository productRepository;
    private final ProductAvailabilityRepository availabilityRepository;

    public DataSeeder(UnitRepository unitRepository, ProductRepository productRepository,
                       ProductAvailabilityRepository availabilityRepository) {
        this.unitRepository = unitRepository;
        this.productRepository = productRepository;
        this.availabilityRepository = availabilityRepository;
    }

    @Override
    public void run(String... args) {
        Unit recife = newUnit("Raizes Recife Boa Viagem", "Recife", "Nordeste", true);
        Unit saoPaulo = newUnit("Raizes SP Paulista", "Sao Paulo", "Sudeste", false);

        Product tapioca = newProduct("Tapioca de queijo coalho", new BigDecimal("18.90"), false);
        Product cuscuz = newProduct("Cuscuz recheado", new BigDecimal("16.50"), false);
        Product boloMacaxeira = newProduct("Bolo de macaxeira", new BigDecimal("9.90"), false);
        Product itemJunino = newProduct("Canjica junina", new BigDecimal("12.00"), true);

        makeAvailable(recife, tapioca, 50);
        makeAvailable(recife, cuscuz, 30);
        makeAvailable(recife, boloMacaxeira, 20);
        makeAvailable(recife, itemJunino, 15);

        makeAvailable(saoPaulo, tapioca, 40);
        makeAvailable(saoPaulo, boloMacaxeira, 25);
        // Sao Paulo opera em formato reduzido: sem cuscuz e sem item sazonal nordestino
    }

    private Unit newUnit(String name, String city, String region, boolean fullKitchen) {
        Unit unit = new Unit();
        unit.setName(name);
        unit.setCity(city);
        unit.setRegion(region);
        unit.setFullKitchen(fullKitchen);
        return unitRepository.save(unit);
    }

    private Product newProduct(String name, BigDecimal price, boolean seasonal) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setSeasonal(seasonal);
        return productRepository.save(product);
    }

    private void makeAvailable(Unit unit, Product product, int stock) {
        ProductAvailability availability = new ProductAvailability();
        availability.setUnit(unit);
        availability.setProduct(product);
        availability.setAvailable(true);
        availability.setStockQuantity(stock);
        availabilityRepository.save(availability);
    }
}
