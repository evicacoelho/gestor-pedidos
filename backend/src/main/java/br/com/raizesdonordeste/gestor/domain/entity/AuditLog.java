package br.com.raizesdonordeste.gestor.domain.entity;

import br.com.raizesdonordeste.gestor.domain.enums.AuditAction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;


@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Column(nullable = false)
    private String reason;

    @Column(name = "performed_by", nullable = false)
    private String performedBy;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt = Instant.now();
}
