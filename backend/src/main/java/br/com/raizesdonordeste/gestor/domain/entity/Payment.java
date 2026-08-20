package br.com.raizesdonordeste.gestor.domain.entity;

import br.com.raizesdonordeste.gestor.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "order_id", unique = true)
    private Order order;

    @Column(name = "external_reference", nullable = false, unique = true)
    private String externalReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDENTE;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "processed_at")
    private Instant processedAt;
}
