package br.com.raizesdonordeste.gestor.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;


@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "lgpd_consent_given", nullable = false)
    private boolean lgpdConsentGiven;

    @Column(name = "lgpd_consent_timestamp")
    private Instant lgpdConsentTimestamp;

    @Column(name = "anonymized", nullable = false)
    private boolean anonymized = false;
}
