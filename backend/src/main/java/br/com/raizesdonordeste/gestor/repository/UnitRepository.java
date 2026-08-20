package br.com.raizesdonordeste.gestor.repository;

import br.com.raizesdonordeste.gestor.domain.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitRepository extends JpaRepository<Unit, Long> {
}
