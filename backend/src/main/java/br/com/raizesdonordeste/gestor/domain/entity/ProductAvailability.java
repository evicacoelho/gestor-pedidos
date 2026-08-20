package br.com.raizesdonordeste.gestor.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "product_availability", uniqueConstraints = @UniqueConstraint(columnNames = {"unit_id", "product_id"}))
@Getter
@Setter
@NoArgsConstructor
public class ProductAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private boolean available;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;
}
