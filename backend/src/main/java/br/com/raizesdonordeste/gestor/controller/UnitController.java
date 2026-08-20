package br.com.raizesdonordeste.gestor.controller;

import br.com.raizesdonordeste.gestor.domain.entity.Unit;
import br.com.raizesdonordeste.gestor.repository.UnitRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/units")
public class UnitController {

    private final UnitRepository unitRepository;

    public UnitController(UnitRepository unitRepository) {
        this.unitRepository = unitRepository;
    }

    @GetMapping
    public List<Unit> list() {
        return unitRepository.findAll();
    }
}
